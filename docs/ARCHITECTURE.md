# 아키텍처 가이드

`Java 21` | `Spring Boot 3.5.9`

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

### Controller (Presentation Layer)

**책임**:
- HTTP 요청/응답 처리
- 입력 검증 (`@Valid`)
- 비즈니스 로직 포함 금지

**예제**:

```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // 데이터가 있는 성공 응답
  @GetMapping("/{id}")
  public CommonApiResponse<UserResponseDto> getUser(@PathVariable Long id) {
    UserResponseDto user = userService.getUser(id);
    return CommonApiResponse.success(SuccessCode.SUCCESS, user);
  }

  // 데이터가 없는 성공 응답 (생성, 삭제 등)
  @PostMapping
  public CommonApiResponse<Void> createUser(@Valid @RequestBody UserRequestDto request) {
    userService.createUser(request);
    return CommonApiResponse.success(SuccessCode.SUCCESS);
  }
}
```

---

### Service (Application Layer)

**책임**:
- 비즈니스 로직 처리
- 트랜잭션 관리 (`@Transactional`)
- 여러 Repository 조합

**예제**:

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDto createUser(UserRequestDto request) {
        // 1. Request DTO → Entity 변환
        User user = request.toEntity();

        // 2. Entity 저장
        User savedUser = userRepository.save(user);

        // 3. Entity → Response DTO 변환
        return UserResponseDto.from(savedUser);
    }
}
```

---

### Repository (Infrastructure Layer)

**책임**:
- 데이터 접근만 담당
- JPA 메서드 네이밍 규칙 준수
- 복잡한 쿼리는 `@Query` 또는 QueryDSL 사용

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


