# API 컨벤션

## 목차

- [1. 공통 응답 (API Response) 컨벤션](#1-공통-응답-api-response-컨벤션)
- [2. 예외 처리 컨벤션](#2-예외-처리-컨벤션)
- [3. Swagger 컨벤션](#3-swagger-컨벤션)
- [관련 문서](#관련-문서)

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

