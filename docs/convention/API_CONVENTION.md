# API 컨벤션

## 목차

- [1. 공통 응답 (API Response) 컨벤션](#1-공통-응답-api-response-컨벤션)
- [2. 예외 처리 컨벤션](#2-예외-처리-컨벤션)
- [3. Swagger 컨벤션](#3-swagger-컨벤션)

## 1. 공통 응답 (API Response) 컨벤션

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

2. **Controller에서 사용**

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

## 2. 예외 처리 컨벤션

**도메인별 ErrorCode와 Exception을 사용하여 관리합니다.**

### 예외 처리 구조

```
GlobalExceptionHandler
    ├── BaseException (커스텀 비즈니스 예외)
    │   ├── UserException (유저 도메인 예외)
    │   ├── ReviewException (리뷰 도메인 예외)
    │   └── ...
    ├── MethodArgumentNotValidException (@Valid 검증 실패)
    ├── IllegalArgumentException (도메인 검증 실패)
    ├── HttpMessageNotReadableException (JSON 파싱 실패)
    └── Exception (그 외 모든 예외)
```

### Error Code 추가 방법

**에러 코드는 도메인별로 분리하여 관리합니다.**

1. **공통 에러 코드** (`ErrorCode.java`)

```java
public enum ErrorCode implements ErrorType {
    // 공통 에러 (C001~C099)
    INVALID_INPUT("C001", "입력값이 올바르지 않습니다", 400),
    INVALID_FORMAT("C002", "데이터 형식이 올바르지 않습니다", 400),
    INTERNAL_SERVER_ERROR("C999", "서버 내부 오류가 발생했습니다", 500);

    private final String code;
    private final String message;
    private final int status;

    ErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }

    @Override
    public int getStatus() { return status; }
}
```

2. **도메인별 에러 코드** (`UserErrorCode.java`)

```java
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorType {
    // User 도메인 에러 (U001 ~ U099)
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", 404),
    INVALID_USER_NAME("U002", "유효하지 않은 사용자 이름입니다", 400),
    INVALID_USER_AGE("U003", "유효하지 않은 나이입니다 (1-150세)", 400),
    USER_ALREADY_EXISTS("U004", "이미 존재하는 사용자입니다", 409);

    private final String code;
    private final String message;
    private final int status;
}
```

3. **도메인별 Exception 클래스** (`UserException.java`)

```java
public class UserException extends BaseException {
    public UserException(UserErrorCode errorCode) {
        super(errorCode);
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
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return UserResponseDto.from(user);
    }

    public void validateUserAge(int age) {
        if (age < 1 || age > 150) {
            throw new UserException(UserErrorCode.INVALID_USER_AGE);
        }
    }
}
```

**코드 네이밍 규칙**

- `C###`: 공통 에러 코드 (Common)
- `U###`: 유저 관련 (User)
- 각 도메인별로 001부터 099까지 할당

### 에러 응답 형식

**도메인별 에러 응답**

```json
{
  "code": "U001",
  "message": "사용자를 찾을 수 없습니다",
  "data": null
}
```

**Validation 실패 시**

```json
{
  "code": "C001",
  "message": "입력값이 올바르지 않습니다",
  "data": {
    "email": "올바른 이메일 형식이 아닙니다",
    "name": "이름은 필수입니다"
  }
}
```

## 3. Swagger 컨벤션

**Swagger를 통해 자동으로 API 문서가 생성됩니다.**

### Swagger 접속

- 개발 환경: `http://localhost:8080/swagger-ui.html`
- 배포 환경: `${SWAGGER_BASE_URL}/swagger-ui.html`

### Controller에 Swagger 어노테이션 추가

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "사용자 조회",
        description = "사용자 ID로 사용자 정보를 조회합니다."
    )
    @ApiExceptions({UserErrorCode.class, ErrorCode.class})
    @GetMapping("/{id}")
    public CommonApiResponse<UserResponseDto> getUser(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        UserResponseDto response = userService.getUser(id);
        return CommonApiResponse.success(SuccessCode.SUCCESS, response);
    }

    @Operation(
        summary = "사용자 정보 수정",
        description = "사용자의 이름 또는 나이를 수정합니다. 제공된 필드만 수정됩니다."
    )
    @ApiExceptions({UserErrorCode.class, ErrorCode.class})
    @PatchMapping("/{id}")
    public CommonApiResponse<UserResponseDto> updateUser(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long id,
        @Valid @RequestBody UserUpdateRequestDto request
    ) {
        UserResponseDto response = userService.updateUser(id, request);
        return CommonApiResponse.success(SuccessCode.SUCCESS, response);
    }
}
```

**`@ApiExceptions` 어노테이션:**
- 해당 API에서 발생 가능한 에러 코드들을 지정
- Swagger 문서에 자동으로 에러 응답 예시가 생성됨
- HTTP 상태 코드별로 그룹화되어 표시

**예시:**
```java
@ApiExceptions({UserErrorCode.class, ErrorCode.class})
```
위 코드는 Swagger에서 다음과 같이 표시됩니다:
- 400 Bad Request: `INVALID_INPUT`, `INVALID_FORMAT` 등
- 404 Not Found: `USER_NOT_FOUND`
- 500 Internal Server Error: `INTERNAL_SERVER_ERROR`

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
| `@Operation` | Controller 메서드 | API 설명 (summary, description) |
| `@ApiExceptions` | Controller 메서드 | 발생 가능한 에러 코드 지정 (자동 문서화) |
| `@Parameter` | 메서드 파라미터 | 파라미터 설명 (description, required, example) |
| `@Schema` | DTO 클래스/필드 | 스키마 설명 |

