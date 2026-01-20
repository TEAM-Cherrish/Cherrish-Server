# 시술명 검색 기능: Elasticsearch 대안 검토 보고서

## 1. 현황 분석

### 1.1 요구사항 요약 (es.md 기준)
| 항목 | 내용 |
|------|------|
| 검색 대상 | `Procedure.name` 단일 필드 (한글 시술명) |
| 핵심 기능 | 오타 보정, 띄어쓰기 무시, 동의어 매칭 |
| 데이터 규모 | 수백~수천 건 (미용 시술 목록) |
| 검색 빈도 | 사용자 시술 등록 시 1회성 |

### 1.2 ES 도입의 오버헤드
- 별도 ES 서버/클러스터 운영 필요 (최소 2GB RAM)
- 데이터 동기화 로직 구현
- 모니터링/장애 대응 포인트 증가
- 비용: AWS OpenSearch t3.small.search 기준 월 ~$30

### 1.3 핵심 질문
> **수백 건의 시술명 검색을 위해 ES 인프라가 정말 필요한가?**

---

## 2. 대안 비교

### 2.1 대안 요약표

| 대안 | 추가 서버 | 구현 복잡도 | 오타 보정 | 동의어 | 한글 지원 | 추천도 |
|------|:--------:|:----------:|:--------:|:------:|:---------:|:------:|
| **PostgreSQL pg_trgm** | X | 낮음 | O | O | O | ★★★★★ |
| **앱 레벨 퍼지매칭** | X | 중간 | O | O | O | ★★★★☆ |
| Hibernate Search (임베디드) | X | 높음 | O | O | △ | ★★★☆☆ |
| MeiliSearch/Typesense | O | 낮음 | O | O | O | ★★★☆☆ |
| Algolia (SaaS) | X | 낮음 | O | O | O | ★★☆☆☆ |
| OpenAI Embedding | X | 중간 | O | O | O | ★★☆☆☆ |
| **Elasticsearch** | O | 중간 | O | O | O | ★★☆☆☆ |

---

## 3. 대안 상세 분석

### 3.1 PostgreSQL pg_trgm (권장)

PostgreSQL에 내장된 트라이그램 기반 유사도 검색 확장.

**장점:**
- 추가 인프라 불필요 (이미 PostgreSQL 사용 중)
- 오타/띄어쓰기 보정 자연스럽게 지원
- 유사도 점수 기반 정렬 가능
- 인덱스 지원으로 성능 우수

**단점:**
- 동의어는 별도 테이블로 관리 필요
- 형태소 분석 없음 (but 시술명은 고유명사라 큰 문제 없음)

**구현 예시:**
```sql
-- 확장 활성화
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 인덱스 생성
CREATE INDEX idx_procedure_name_trgm ON procedures USING gin (name gin_trgm_ops);

-- 유사도 검색 (오타 보정)
SELECT id, name, similarity(name, '프락셀') AS score
FROM procedures
WHERE similarity(name, '프락셀') > 0.3
ORDER BY score DESC;

-- 또는 LIKE와 유사하지만 오타 허용
SELECT * FROM procedures
WHERE name % '울세라';  -- % 연산자: 유사도 임계값 이상
```

**동의어 처리:**
```sql
-- 동의어 테이블
CREATE TABLE procedure_synonyms (
    id BIGSERIAL PRIMARY KEY,
    canonical_name VARCHAR(100) NOT NULL,  -- 대표어
    synonym VARCHAR(100) NOT NULL           -- 동의어
);

-- 검색 쿼리 (동의어 포함)
SELECT DISTINCT p.* FROM procedures p
LEFT JOIN procedure_synonyms s ON p.name = s.canonical_name
WHERE p.name % :keyword
   OR s.synonym % :keyword
ORDER BY GREATEST(
    similarity(p.name, :keyword),
    COALESCE(similarity(s.synonym, :keyword), 0)
) DESC;
```

**QueryDSL 연동:**
```java
// Native Query 또는 Custom Function 등록 필요
@Query(value = """
    SELECT p.* FROM procedures p
    WHERE similarity(p.name, :keyword) > 0.3
    ORDER BY similarity(p.name, :keyword) DESC
    """, nativeQuery = true)
List<Procedure> searchByNameFuzzy(@Param("keyword") String keyword);
```

**예상 작업량:** 1-2일

---

### 3.2 애플리케이션 레벨 퍼지 매칭

데이터를 메모리에 캐싱하고 Java에서 직접 유사도 계산.

**장점:**
- 완전한 제어 가능 (커스텀 로직)
- 외부 의존성 없음
- 수백 건 데이터에 매우 적합

**단점:**
- 직접 구현 필요
- 데이터 캐시 관리 필요

**구현 예시:**
```java
@Service
@RequiredArgsConstructor
public class ProcedureSearchService {

    private final ProcedureRepository procedureRepository;
    private final SynonymRepository synonymRepository;

    @Cacheable("procedures")
    public List<ProcedureSearchDto> getAllProceduresForSearch() {
        return procedureRepository.findAll().stream()
            .map(p -> new ProcedureSearchDto(p.getId(), p.getName()))
            .toList();
    }

    public List<Procedure> search(String keyword) {
        String normalizedKeyword = normalize(keyword);

        // 1. 동의어 변환
        String canonicalKeyword = synonymRepository
            .findCanonicalName(normalizedKeyword)
            .orElse(normalizedKeyword);

        // 2. 퍼지 매칭
        return getAllProceduresForSearch().stream()
            .map(p -> new ScoredResult(p, calculateScore(p.name(), canonicalKeyword)))
            .filter(r -> r.score() > 0.3)
            .sorted(Comparator.comparing(ScoredResult::score).reversed())
            .map(r -> procedureRepository.findById(r.procedure().id()).orElseThrow())
            .toList();
    }

    private double calculateScore(String target, String keyword) {
        // Levenshtein 기반 유사도
        int distance = LevenshteinDistance.getDefaultInstance()
            .apply(normalize(target), keyword);
        int maxLen = Math.max(target.length(), keyword.length());
        return 1.0 - ((double) distance / maxLen);
    }

    private String normalize(String s) {
        return s.toLowerCase().replaceAll("\\s+", "");
    }
}
```

**의존성 추가:**
```gradle
implementation 'org.apache.commons:commons-text:1.11.0'  // LevenshteinDistance
```

**예상 작업량:** 2-3일

---

### 3.3 Hibernate Search (Lucene 임베디드)

Lucene을 애플리케이션에 임베딩하여 Full-Text Search 제공.

**장점:**
- 별도 서버 불필요 (임베디드 모드)
- ES와 유사한 기능 (Fuzzy, 동의어, 형태소 분석)
- JPA 엔티티와 자동 동기화

**단점:**
- 한글 형태소 분석기(Nori) 설정 복잡
- 학습 곡선 있음
- 로컬 인덱스 파일 관리 필요

**구현 예시:**
```java
@Entity
@Indexed
public class Procedure {
    @Id
    @GeneratedValue
    private Long id;

    @FullTextField(analyzer = "korean")
    private String name;
}

// 검색
SearchResult<Procedure> result = searchSession.search(Procedure.class)
    .where(f -> f.match()
        .field("name")
        .matching(keyword)
        .fuzzy(1))
    .fetchAll();
```

**예상 작업량:** 3-5일

---

### 3.4 MeiliSearch / Typesense

ES보다 가벼운 검색 엔진. 여전히 별도 프로세스 필요.

**장점:**
- ES 대비 리소스 사용량 1/10
- 설정 간단, 한글 기본 지원
- 오타 보정 기본 내장

**단점:**
- 여전히 별도 서버 필요
- Docker 컨테이너 추가 운영

**리소스 비교:**
| 항목 | Elasticsearch | MeiliSearch |
|------|--------------|-------------|
| 최소 RAM | 2GB | 256MB |
| Docker 이미지 | ~800MB | ~150MB |

**예상 작업량:** 1-2일 (인프라 포함)

---

### 3.5 Algolia (SaaS)

완전 관리형 검색 서비스.

**장점:**
- 인프라 관리 불필요
- 뛰어난 한글 지원
- SDK 제공

**단점:**
- 비용 (월 $0 ~ $35+ 사용량 기반)
- 외부 서비스 의존성
- 데이터 외부 전송

**예상 작업량:** 1일

---

### 3.6 OpenAI Embedding 활용 (실험적)

이미 Spring AI를 사용 중이므로, 시술명을 벡터화하여 의미 기반 검색.

**장점:**
- 의미적 유사도 검색 가능 ("피부 재생" → "리쥬란" 매칭)
- 기존 OpenAI 인프라 활용

**단점:**
- API 호출 비용/지연
- 오버엔지니어링 가능성
- 간단한 오타 보정에는 과함

**예상 작업량:** 2-3일

---

## 4. 권장 사항

### 4.1 최종 권장: PostgreSQL pg_trgm

**이유:**
1. **추가 인프라 제로**: 이미 PostgreSQL 사용 중
2. **충분한 기능**: 오타 보정 + 유사도 검색 기본 제공
3. **낮은 구현 복잡도**: SQL 확장 활성화 + 인덱스 생성 + 쿼리 수정
4. **검증된 기술**: 프로덕션에서 널리 사용됨
5. **데이터 규모에 적합**: 수백~수천 건에 최적

### 4.2 구현 로드맵

```
1단계: pg_trgm 확장 활성화 및 인덱스 생성
2단계: 동의어 테이블 설계 및 초기 데이터 입력
3단계: 검색 쿼리 구현 (Native Query)
4단계: 기존 LIKE 검색 대체
5단계: 테스트 및 임계값 튜닝
```

### 4.3 ES가 필요해지는 시점

다음 상황이 발생하면 ES 도입 재검토:
- 검색 대상 필드가 3개 이상으로 확장
- 데이터가 10만 건 이상으로 증가
- 복잡한 형태소 분석이 필요한 경우 (리뷰 검색 등)
- 검색 로그 분석/자동완성 등 고급 기능 필요

---

## 5. 결론

| 기준 | ES | pg_trgm (권장) |
|------|:--:|:--------------:|
| 추가 서버 | 필요 | 불필요 |
| 월 비용 | $30+ | $0 |
| 구현 복잡도 | 중간 | 낮음 |
| 요구사항 충족 | O | O |
| 확장성 | 높음 | 중간 |

**현재 요구사항(시술명 단일 필드, 수백 건 데이터)에는 pg_trgm이 가장 효율적인 선택입니다.**

ES는 좋은 기술이지만, 현재 규모에서는 "대포로 모기 잡기"에 해당합니다. 추후 검색 요구사항이 복잡해지면 그때 도입을 검토해도 늦지 않습니다.
