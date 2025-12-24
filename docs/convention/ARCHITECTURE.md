# 아키텍처 가이드

이 문서는 Cherrish 프로젝트의 전체 아키텍처와 레이어별 책임을 설명합니다.

## 목차
- [패키지 구조](#패키지-구조)
- [레이어별 책임](#레이어별-책임)

---

## 패키지 구조

프로젝트는 **DDD (Domain-Driven Design) 기반의 레이어드 아키텍처**를 따릅니다.

```
src/
├── main/
│   ├── java/
│   │   └── com.sopt.cherrish/
│   │       ├── global/                      # 공통 설정
│   │       │   ├── config/
│   │       │   ├── exception/
│   │       │   ├── response/
│   │       │   └── util/
│   │       │
│   │       ├── domain/                      # 도메인 영역
│   │       │   ├── user/                    # User 도메인
│   │       │   │   ├── presentation/          # Presentation Layer (표현 계층)
│   │       │   │   │   ├── UserController.java
│   │       │   │   │   └── dto/
│   │       │   │   │       ├── request/
│   │       │   │   │       └── response/
│   │       │   │   │
│   │       │   │   ├── application/           # Application Layer (응용 계층)
│   │       │   │   │   ├── service/
│   │       │   │   │   └── facade/           # 여러 도메인 서비스 조합
│   │       │   │   │
│   │       │   │   ├── domain/                # Domain Layer (도메인 계층)
│   │       │   │   │   ├── model/            # 도메인 모델/엔티티
│   │       │   │   │   ├── service/          # 도메인 서비스
│   │       │   │   │   ├── repository/       # 리포지토리 인터페이스
│   │       │   │   │   └── vo/               # Value Object
│   │       │   │   │
│   │       |   |   ├── infrastructure/        # Infrastructure Layer (인프라 계층)
│   │       |   │   │   ├── persistence/      # 영속성 구현
│   │       │   |   │   │   ├── entity/       # JPA 엔티티
│   │       │   |   │   │   └── repository/   # 리포지토리 구현체
│   │       │   |   │   └── external/         # 외부 API 연동
│   │       │   │
│   │       │   ├── post/                    # Post 도메인
│   │       │   │   ├── presentation/
│   │       │   │   ├── application/
│   │       │   │   ├── domain/
│   │       │   │   └── infrastructure/
│   │       │   │
│   │       │   └── comment/                 # Comment 도메인
│   │       │       ├── presentation/
│   │       │       ├── application/
│   │       │       ├── domain/
│   │       │       └── infrastructure/
│   │       │
│   │       └── Application.java
│   └── resources/
│       ├── application.yml
│       └── application-{profile}.yml
└── test/
```

---

## 레이어별 책임

### Presentation Layer (Controller)

**책임**:
- HTTP 요청/응답 처리
- 입력 검증 (`@Valid`)
- 비즈니스 로직 포함 금지

**✅ 해야 할 일**:
- HTTP 상태 코드 및 응답 형식 결정
- 요청 데이터 검증 (`@Valid`, `@PathVariable`, `@RequestParam`)
- Service 계층 호출 및 결과 반환
- API 문서화 (`@Operation`, `@Tag`)

**❌ 하지 말아야 할 일**:
- 비즈니스 로직 작성 (Service로)
- 데이터베이스 직접 접근 (Repository는 Service에서만)
- 트랜잭션 관리 (Service에서 관리)
- 예외 직접 처리 (GlobalExceptionHandler가 처리)

**예제**:

```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관련 API")
public class UserController {

  private final UserService userService;

  // 데이터가 있는 성공 응답
  @Operation(summary = "회원 조회")
  @GetMapping("/{id}")
  public CommonApiResponse<UserResponseDto> getUser(@PathVariable Long id) {
    UserResponseDto user = userService.getUser(id);
    return CommonApiResponse.success(SuccessCode.SUCCESS, user);
  }

  // 데이터가 없는 성공 응답 (생성, 삭제 등)
  @Operation(summary = "회원 생성")
  @PostMapping
  public CommonApiResponse<Void> createUser(@Valid @RequestBody UserRequestDto request) {
    userService.createUser(request);
    return CommonApiResponse.success(SuccessCode.SUCCESS);
  }
}
```

---

### Application Layer (Service)

**책임**:
- 비즈니스 로직 처리
- 트랜잭션 관리 (`@Transactional`)
- 여러 Repository 조합

**✅ 해야 할 일**:
- 핵심 비즈니스 로직 구현
- 트랜잭션 경계 설정 (`@Transactional`)
- 도메인 객체 간 협업 조율
- 유효성 검증 (비즈니스 규칙)
- 여러 Repository 조합하여 복잡한 작업 수행

**❌ 하지 말아야 할 일**:
- HTTP 관련 코드 작성 (Controller에서만)
- SQL 쿼리 직접 작성 (Repository에서)
- Entity를 직접 반환 (DTO 변환 필수)

**주의사항**:
- 클래스 레벨에 `@Transactional(readOnly = true)` 선언 (기본값)
- 쓰기 작업 메서드에만 `@Transactional` 재선언
- 트랜잭션 안에서 외부 API 호출 지양

**예제**:

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 조회는 readOnly
    public UserResponseDto getUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(user);
    }

    // 쓰기 작업은 @Transactional 재선언
    @Transactional
    public UserResponseDto createUser(UserRequestDto request) {
        // 1. 비즈니스 검증
        validateDuplicateEmail(request.getEmail());

        // 2. Request DTO → Entity 변환
        User user = request.toEntity();

        // 3. Entity 저장
        User savedUser = userRepository.save(user);

        // 4. Entity → Response DTO 변환
        return UserResponseDto.from(savedUser);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
```

---

### Infrastructure Layer (Repository)

**책임**:
- 데이터 접근만 담당
- JPA 메서드 네이밍 규칙 준수
- 복잡한 쿼리는 `@Query` 또는 QueryDSL 사용

**✅ 해야 할 일**:
- 데이터베이스 CRUD 작업
- JPA 네이밍 규칙을 따르는 메서드 정의 (`findBy`, `existsBy`, `deleteBy` 등)
- 복잡한 쿼리는 `@Query` 어노테이션 사용
- 성능이 중요한 경우 QueryDSL 사용

**❌ 하지 말아야 할 일**:
- 비즈니스 로직 작성 (Service에서)
- DTO 변환 작업 (Service에서)
- 트랜잭션 관리 (Service에서)

**예제**:

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // JPA 메서드 네이밍 규칙
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByAgeGreaterThan(int age);

    // 복잡한 쿼리는 @Query 사용
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% AND u.isActive = true")
    List<User> searchActiveUsers(@Param("keyword") String keyword);

    // Native Query (필요한 경우만)
    @Query(value = "SELECT * FROM users WHERE created_at > :date", nativeQuery = true)
    List<User> findRecentUsers(@Param("date") LocalDateTime date);
}
```

**JPA 메서드 네이밍 규칙**:
- `findBy...`: 조회
- `existsBy...`: 존재 여부 확인
- `countBy...`: 개수 세기
- `deleteBy...`: 삭제

---

## 레이어 간 의존성 규칙

```
Presentation Layer (Controller)
        ↓
Application Layer (Service)
        ↓
Domain Layer (Repository Interface)
        ↓
Infrastructure Layer (Repository Implementation)
```

**원칙**:
- 상위 레이어는 하위 레이어에 의존할 수 있음
- 하위 레이어는 상위 레이어에 의존하면 안 됨
- 각 레이어는 자신의 책임만 수행


