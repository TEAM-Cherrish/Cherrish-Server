`Java - 21`

## 1. 패키지 구조

```
src/
├── main/
│   ├── java/
│   │   └── com.sopt.example/
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

## 2. 네이밍 컨벤션

**클래스명**

- Controller: `UserController`, `OrderController`
- Service: `UserService`, `OrderService`
- Repository: `UserRepository`, `OrderRepository`
- DTO: `UserRequestDto`, `UserResponseDto`
- Entity: `User`, `Order`

**메서드명**

- 조회: `getUser()`, `findUserById()`, `findUserList()`
- 생성: `createUser()`, `saveUser()`
- 수정: `updateUser()`, `modifyUser()`
- 삭제: `deleteUser()`, `removeUser()`
- 검증: `validateUser()`, `checkUserExists()`

**변수명**

- 카멜케이스 사용: `userId`, `userName`
- boolean 타입: `isActive`, `hasPermission`
- Collection: 복수형 사용: `users`

## 3. 레이어별 책임

**Controller**

- HTTP 요청/응답 처리
- 입력 검증 (`@Valid`)
- 비즈니스 로직 포함 금지

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

**Service**

- 비즈니스 로직 처리
- 트랜잭션 관리 (`@Transactional`)
- 여러 Repository 조합

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

**Repository**

- 데이터 접근만 담당
- JPA 메서드 네이밍 규칙 준수
- 복잡한 쿼리는 `@Query` 또는 QueryDSL 사용

## 4. DTO 작성 규칙

```java
// Request DTO
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRequestDto {

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    // DTO → Entity 변환
    public User toEntity() {
        return User.builder()
                .name(this.name)
                .email(this.email)
                .build();
    }
}

// Response DTO
@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    // Entity → DTO 변환
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

```

## 5. Entity 작성 규칙

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder
    private User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // 비즈니스 로직은 Entity 내부에 작성
    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }
}

```

**Entity 규칙**

- `@Setter` 사용 금지 (불변성 보장)
- `@Getter` 는 쓸 때 한번 고민해보기!
- 생성자는 `@Builder` 사용
- 비즈니스 로직은 Entity 내부에 작성

## 6. Configuration 작성

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 설정 내용
        return http.build();
    }
}

```

- `@Configuration` 클래스는 `global/config` 패키지에 위치
- 각 설정은 목적별로 분리 (Security, Database, Redis 등)

## 7. Git Commit 컨벤션

| 커밋 유형 | 의미 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `style` | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우 |
| `refactor` | 코드 리팩토링 |
| `test` | 테스트 코드, 리팩토링 테스트 코드 추가 |
| `chore` | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore |
| `rename` | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우 |
| `remove` | 파일을 삭제하는 작업만 수행한 경우 |
| `!HOTFIX` | 급하게 치명적인 버그를 고쳐야 하는 경우 |

```
feat (domain): 새로운 기능 추가
fix (domain): 버그 수정
refactor (domain): 코드 리팩토링
style (domain): 코드 포맷팅, 세미콜론 누락 등
docs: 문서 수정
test: 테스트 코드 추가/수정
chore: 빌드 업무, 패키지 매니저 수정
```

**예시**: feat (User): 로그인 기능 추가

## 8. Git Branch 컨벤션

```smalltalk
<이슈번호>-<커밋 유형>/내용

Git Flow

main(배포 버전 코드)
dev(개발 단계 코드)

// 영어로 쓰기
15-feature/implement-user-login

```

## 9. Issue 잘 만들기!

```java
템플릿에 맞춰서

버그 찾으면 버그 이슈화
기능 추가 티켓 다 이슈화
```

## 10. 환경 설정 관리

```yaml
# application.yml
spring:
  profiles:
    active: ${PROFILE:dev}

# application-dev.yml (개발)
# application-prod.yml (운영)

```

- 민감 정보는 환경 변수로 관리
- `.env` 파일은 `.gitignore`에 추가

## 11. 공통 응답 (API Response) 컨벤션

**모든 API는 `CommonApiResponse`로 통일된 응답 형식을 사용합니다.**

### 응답 구조

```json
{
  "code": "S200",
  "message": "성공",
  "data": { ... }
}

```

### Success Code 추가 방법

**SuccessCode는 global에서 통합 관리합니다.**

1. **공통 성공 코드** (`SuccessCode.java`)

```java
@Getter
public enum SuccessCode implements SuccessType {
	// 공통 응답 코드
    SUCCESS("S200", "성공"),

	// 리뷰 응답 코드
    REVIEW_CREATED("R201", "리뷰 작성 성공");

    private final String code;
    private final String message;

    SuccessCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

```

1. **Controller에서 사용**

```java
@GetMapping("/{id}")
public CommonApiResponse<UserResponseDto> getUser(@PathVariable Long id) {
    UserResponseDto user = userService.getUser(id);
    return CommonApiResponse.success(SuccessCode.MEMBER_RETRIEVED, user);
}

```

**코드 네이밍 규칙**

- `S###`: 공통 성공 코드
- `R###`: 리뷰 관련
- `C###`: 카테고리 관련
- `B###`: Book 관련
- 각 도메인별로 001부터 099까지 할당

## 12. 예외 처리 컨벤션

**global ErrorCode와 BaseException을 사용하여 관리합니다.**

### 예외 처리 구조

```
GlobalExceptionHandler
    ├── BaseException (커스텀 비즈니스 예외)
    ├── MethodArgumentNotValidException (@Valid 검증 실패)
    ├── IllegalArgumentException (도메인 검증 실패)
    ├── HttpMessageNotReadableException (JSON 파싱 실패)
    └── Exception (그 외 모든 예외)

```

### Error Code 추가 방법

1. **에러 코드** (`ErrorCode.java`)

```java
@Getter
public enum ErrorCode implements ErrorType {
    // 공통 에러 (C001~C099)
    INVALID_INPUT("E001", "입력값이 올바르지 않습니다", 400),
    INVALID_FORMAT("E002", "데이터 형식이 올바르지 않습니다", 400),
    INTERNAL_SERVER_ERROR("E999", "서버 내부 오류가 발생했습니다", 500),

	// 리뷰 에러
    REVIEW_NOT_FOUND("R001", "리뷰를 찾을 수 없습니다", 404);

    private final String code;
    private final String message;
    private final int status;

    ErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}

```

### Service에서 예외 발생

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto getUser(Long id) {
        // 도메인별 Exception과 ErrorCode 사용
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return UserResponseDto.from(user);
    }

    public void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            // 커스텀 메시지와 함께 예외 발생
            throw new BaseException(ErrorCode.DUPLICATE_EMAIL, email);
        }
    }
}

```

**코드 네이밍 규칙**

- `E###`: 공통 에러 코드
- `R###`: 리뷰 관련
- `C###`: 카테고리 관련
- `B###`: Book 관련
- 각 도메인별로 001부터 099까지 할당

### 에러 응답 형식

```json
{
  "code": "M001",
  "message": "사용자를 찾을 수 없습니다",
  "data": null
}

```

**Validation 실패 시**

```json
{
  "code": "E001",
  "message": "입력값이 올바르지 않습니다",
  "data": {
    "email": "올바른 이메일 형식이 아닙니다",
    "name": "이름은 필수입니다"
  }
}

```

## 13. Swagger 컨벤션

**Swagger를 통해 자동으로 API 문서가 생성됩니다.**

### Swagger 접속

- 개발 환경: `http://localhost:8080/swagger-ui.html`
- 배포 환경: `${SWAGGER_BASE_URL}/swagger-ui.html`

### Controller에 Swagger 어노테이션 추가

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 정보 조회", description = "ID로 회원 정보를 조회합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = CommonApiResponse.class)))
    })
    @GetMapping("/{id}")
    public CommonApiResponse<UserResponseDto> getUser(
            @Parameter(description = "회원 ID", required = true) @PathVariable Long id) {
        UserResponseDto user = userService.getUser(id);
        return CommonApiResponse.success(SuccessCode.SUCCESS, user);
    }

    @Operation(summary = "회원 가입", description = "새로운 회원을 생성합니다")
    @PostMapping
    public CommonApiResponse<Void> createUser(
            @Valid @RequestBody UserRequestDto request) {
        userService.createUser(request);
        return CommonApiResponse.success(SuccessCode.SUCCESS);
    }
}

```

### DTO에 Swagger 어노테이션 추가

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "회원 생성 요청")
public class UserRequestDto {

    @Schema(description = "회원 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Schema(description = "이메일 주소", example = "hong@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
}

@Getter
@Builder
@Schema(description = "회원 정보 응답")
public class UserResponseDto {

    @Schema(description = "회원 ID", example = "1")
    private Long id;

    @Schema(description = "회원 이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    private String email;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}

```

### Swagger 어노테이션 정리

| 어노테이션 | 사용 위치 | 설명 |
| --- | --- | --- |
| `@Tag` | Controller 클래스 | API 그룹 정의 |
| `@Operation` | Controller 메서드 | API 설명 |
| `@ApiResponses` | Controller 메서드 | 응답 코드별 설명 |
| `@Parameter` | 메서드 파라미터 | 파라미터 설명 |
| `@Schema` | DTO 클래스/필드 | 스키마 설명 |

## 14. 코드 스타일 가이드 (Checkstyle)

**프로젝트는 Checkstyle을 통해 코드 스타일을 자동으로 검사합니다.**

### 크기 제한

**파일 및 코드 길이**

```java
// 파일 최대 길이: 500줄
// 한 파일이 너무 길어지면 클래스 분리를 고려

// 메서드 최대 길이: 150줄
public void processOrder() {
    // 메서드가 너무 길어지면 여러 메서드로 분리
}

// 라인 최대 길이: 140자
// 한 줄이 너무 길면 가독성이 떨어지므로 적절히 줄바꿈

// 메서드 파라미터 최대 개수: 7개
public void createUser(String name, String email, int age, ...) {
    // 파라미터가 많으면 DTO로 묶는 것을 고려
}
```

### Import 규칙

```java
// ❌ 나쁜 예: 와일드카드 import 금지
import java.util.*;
import com.sopt.cherrish.domain.*;

// ✅ 좋은 예: 명시적 import
import java.util.List;
import java.util.ArrayList;
import com.sopt.cherrish.domain.User;
```

**이유**: 와일드카드 import는 어떤 클래스를 사용하는지 불명확하고, 네임스페이스 충돌 가능성이 있음

### 중괄호 규칙

**모든 제어문에 중괄호 필수**

```java
// ❌ 나쁜 예: 중괄호 생략
if (condition)
    doSomething();

for (int i = 0; i < 10; i++)
    process(i);

// ✅ 좋은 예: 중괄호 사용
if (condition) {
    doSomething();
}

for (int i = 0; i < 10; i++) {
    process(i);
}
```

**이유**: 코드 수정 시 실수 방지, 가독성 향상

### 로거 사용 규칙

**Lombok @Slf4j 사용**

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    public void processUser(Long userId) {
        log.info("Processing user: {}", userId);
        log.warn("User not found: {}", userId);
        log.error("Error processing user", exception);
    }
}
```

**로그 레벨 가이드**

- `log.error()`: 시스템 오류, 예외 상황
- `log.warn()`: 경고, 비정상적이지만 처리 가능한 상황
- `log.info()`: 주요 비즈니스 로직 흐름
- `log.debug()`: 개발 시 디버깅 정보

### 공백 및 포맷팅

**연산자 주변 공백**

```java
// ✅ 좋은 예
int sum = a + b;
boolean result = (x > 0) && (y < 10);
String name = "Hello" + " World";

// ❌ 나쁜 예
int sum=a+b;
boolean result=(x>0)&&(y<10);
```

**괄호 패딩**

```java
// ✅ 좋은 예
if (condition) {
    method(param1, param2);
}

// ❌ 나쁜 예
if ( condition ) {
    method( param1, param2 );
}
```

### 코딩 규칙

**equals()와 hashCode() 함께 구현**

```java
@Entity
public class User {
    @Override
    public boolean equals(Object o) {
        // equals 구현
    }

    @Override
    public int hashCode() {
        // hashCode도 반드시 함께 구현
    }
}
```

**switch문에 default 필수**

```java
// ✅ 좋은 예
switch (status) {
    case ACTIVE:
        handleActive();
        break;
    case INACTIVE:
        handleInactive();
        break;
    default:
        throw new IllegalArgumentException("Unknown status: " + status);
}
```

**boolean 표현식 단순화**

```java
// ❌ 나쁜 예
if (isActive == true) { }
return isValid == false;

// ✅ 좋은 예
if (isActive) { }
return !isValid;
```

### 배열 선언 스타일

```java
// ✅ 좋은 예: Java 스타일
String[] names;
int[] numbers;

// ❌ 나쁜 예: C 스타일
String names[];
int numbers[];
```

### Checkstyle 설정

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
