# Cherrish 개발 가이드

## 빠른 시작

```bash
# 1. 환경 설정 (처음 한 번만)
cp .env.example .env

# 2. DB 시작
docker-compose up -d

# 3. 앱 실행
./gradlew bootRun
```

**접속**: http://localhost:8080

---

## Docker 기본 명령어

### 시작/종료
```bash
docker-compose up -d              # DB 시작 (백그라운드)
docker-compose down               # DB 종료
docker-compose down -v            # DB 종료 + 데이터 삭제
```

### 모니터링
```bash
docker-compose ps                 # 실행 중인 컨테이너 확인
docker-compose logs -f            # 로그 실시간 확인
docker-compose logs -f postgres   # PostgreSQL 로그만 확인
```

### DB 접속
```bash
docker exec -it cherrish-postgres psql -U postgres -d cherrish

# psql 명령어
\dt                               # 테이블 목록
\d table_name                     # 테이블 구조
SELECT * FROM your_table;         # 쿼리 실행
\q                                # 종료
```

---

## 코드 스타일 검사

### Checkstyle

프로젝트의 Checkstyle 설정은 `config/checkstyle/checkstyle.xml`에 정의되어 있습니다.

**빌드 시 자동 검사**

```bash
# 전체 빌드 (Checkstyle 포함)
./gradlew build

# Checkstyle만 실행
./gradlew checkstyleMain checkstyleTest

# 리포트 확인
# build/reports/checkstyle/main.html
```

**CI/CD 파이프라인**

- PR 생성 시 자동으로 Checkstyle 검사 실행
- Checkstyle 통과해야만 병합 가능
- 최대 경고 허용: 100개 (점진적 개선 목표)
