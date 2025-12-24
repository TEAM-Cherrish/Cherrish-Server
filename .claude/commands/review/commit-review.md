---
description: 특정 커밋부터 현재까지의 변경사항을 종합적으로 코드리뷰합니다
argument-hint: <commit-hash>
allowed-tools: Bash(git:*), Read, Grep, Glob
---

# Commit-to-Current Code Review

Review all changes from a specific commit to the current state (HEAD).

## Context Collection

First, collect changes using git commands:

!`git diff $1 HEAD --name-status`
!`git log --oneline $1..HEAD`
!`git diff $1 HEAD --stat`

## Review Instructions

You are reviewing code for a **Spring Boot 3.5.9 + Java 21 based DDD architecture** project.

### Review Scope
Review all changes from commit **$1** to current HEAD.

**IMPORTANT: All review output must be written in Korean (한글).**

---

## Review Categories

### 1. Convention Compliance

Reference project convention documents:
- @docs/convention/ARCHITECTURE.md
- @docs/convention/API_CONVENTION.md
- @docs/convention/CODE_STYLE.md
- @docs/convention/CODING_GUIDE.md

**Checklist**:
- [ ] **Naming conventions**: Controller/Service/Repository/DTO/Entity suffixes used
- [ ] **Method naming**: get/find (read), create/save (create), update/modify (update), delete/remove (delete)
- [ ] **Checkstyle compliance**:
  - Line length ≤ 140 chars
  - Method length ≤ 150 lines
  - File length ≤ 500 lines
  - Parameter count ≤ 7
- [ ] **Import rules**: No wildcard imports (`import java.util.*` forbidden)
- [ ] **Control statements**: All if/for/while must have braces
- [ ] **Switch statements**: Must have default case
- [ ] **Logging**: Use `@Slf4j`

---

### 2. Package Structure

Verify DDD layer compliance:

**Correct package locations**:
- Controller → `presentation/` package
- Service → `application/service/` or `application/facade/`
- Repository Interface → `domain/repository/`
- Repository Implementation → `infrastructure/persistence/repository/`
- JPA Entity → `infrastructure/persistence/entity/`
- Domain Model → `domain/model/`
- Request DTO → `presentation/dto/request/`
- Response DTO → `presentation/dto/response/`

**Checklist**:
- [ ] Each file is in the correct layer package
- [ ] Independent package structure per domain
- [ ] No domain-specific code in `global/` package

---

### 3. Architectural Boundaries

Verify layer dependency rules:

```
Controller → Service → Repository Interface → Repository Implementation
```

**Controller Layer**:
- [ ] Controller only calls Service (no direct Repository access)
- [ ] No business logic in Controller
- [ ] Only HTTP-related code (request/response handling)

**Service Layer**:
- [ ] No HTTP-related code in Service (no ResponseEntity, HttpStatus)
- [ ] DTO ↔ Entity transformation in Service (not in Controller or Repository)
- [ ] Never return Entity directly (always return DTO)

**Repository Layer**:
- [ ] No business logic in Repository methods (pure data access only)

**Circular dependencies**:
- [ ] No circular dependencies between layers

---

### 4. Transaction Management

**Service class pattern**:
```java
@Service
@Transactional(readOnly = true)  // Class-level default
public class UserService {
    public UserResponseDto getUser(Long id) { ... }  // readOnly

    @Transactional  // Method-level override for writes
    public void createUser(UserRequestDto dto) { ... }
}
```

**Checklist**:
- [ ] Service class has `@Transactional(readOnly = true)` at class level
- [ ] Only write operations have method-level `@Transactional`
- [ ] No external API calls inside transactions
- [ ] Proper exception handling for transaction rollback

---

### 5. API Response Standard

**All endpoints must use `CommonApiResponse<T>`**:

```java
// ✅ Correct usage
@GetMapping("/{id}")
public CommonApiResponse<UserResponseDto> getUser(@PathVariable Long id) {
    UserResponseDto user = userService.getUser(id);
    return CommonApiResponse.success(SuccessCode.SUCCESS, user);
}

// Success response without data
@PostMapping
public CommonApiResponse<Void> createUser(@Valid @RequestBody UserRequestDto request) {
    userService.createUser(request);
    return CommonApiResponse.success(SuccessCode.SUCCESS);
}
```

**Checklist**:
- [ ] All endpoints use `CommonApiResponse<T>` wrapper
- [ ] Appropriate `SuccessCode` used
- [ ] DTOs have Swagger `@Schema` annotations
- [ ] Controllers use `@Valid` for request validation
- [ ] API documented with `@Tag`, `@Operation` annotations

---

### 6. DTO Pattern

**Request DTO**:
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRequestDto {
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    public User toEntity() {
        return User.builder().name(this.name).build();
    }
}
```

**Response DTO**:
```java
@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String name;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
            .id(user.getId())
            .name(user.getName())
            .build();
    }
}
```

**Checklist**:
- [ ] Request DTO: has `toEntity()` method
- [ ] Response DTO: has `static from(Entity)` method
- [ ] Request DTO: `@Getter` + `@NoArgsConstructor(access = PROTECTED)`
- [ ] Response DTO: `@Getter` + `@Builder`
- [ ] Request DTO has validation annotations (@NotBlank, @Email, @Size, etc.)

---

### 7. Exception Handling

**Correct exception handling**:
```java
// Service throws BaseException
if (userRepository.existsByEmail(email)) {
    throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
}

// GlobalExceptionHandler handles automatically
```

**Checklist**:
- [ ] Use `BaseException` + `ErrorCode` combination
- [ ] ErrorCode naming: C### (common), domain prefix (U###, P###, etc.)
- [ ] Service doesn't throw generic Exception
- [ ] ErrorCode defines appropriate HTTP status codes

---

### 8. Security Vulnerabilities

**SQL Injection**:
```java
// ❌ Dangerous: String concatenation
@Query("SELECT u FROM User u WHERE u.email = '" + email + "'")

// ✅ Safe: Parameter binding
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);
```

**Checklist**:
- [ ] **SQL Injection**: No string concatenation in `@Query` (use parameter binding)
- [ ] **XSS**: All Request DTOs have input validation (@NotBlank, @Size, @Pattern)
- [ ] **Sensitive data**: No passwords/tokens exposed in logs
- [ ] **Authentication/Authorization**: Security annotations on protected endpoints (if needed)
- [ ] **Mass Assignment**: DTOs define explicit fields only (no Map acceptance)
- [ ] **Entity exposure**: Controllers never return Entity directly (always use DTO)

---

### 9. Unnecessary Complexity

**Check for over-engineering**:
- [ ] No overly deep inheritance hierarchies
- [ ] No interfaces with single implementation (review if truly needed)
- [ ] No excessive method parameters (> 7)
- [ ] No duplicate code (extractable common logic)
- [ ] No complex conditionals (review if simplifiable)

---

### 10. Code Quality

**Entity pattern**:
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    // ❌ @Setter forbidden

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder
    private User(String name) { this.name = name; }

    // ✅ Business logic inside Entity
    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }
}
```

**Checklist**:
- [ ] equals() and hashCode() implemented together
- [ ] Simplified Boolean expressions (remove `== true`, `== false`)
- [ ] Java-style array declarations (`String[]` not `String array[]`)
- [ ] Entity has no `@Setter` (use Builder and business methods)
- [ ] Think twice before using `@Getter` (is it really needed?)

---

## Output Format

**CRITICAL: Write all review results in Korean (한글).**

Structure your code review as follows:

### 📊 Summary
- **변경 파일 수**: X개
- **추가/삭제 라인**: +X/-Y
- **심각도**: [CRITICAL / HIGH / MEDIUM / LOW]
- **전체 평가**: 한 줄 요약

---

### ⚠️ Issues Found

For each issue:

#### 1. [카테고리] - [심각도] - 파일:라인

**문제점**:
[구체적인 문제 설명]

**현재 코드**:
```java
// 문제가 있는 코드
```

**권장 수정**:
```java
// 수정된 코드
```

**근거**:
[왜 이것이 문제인지, 어떤 컨벤션/원칙을 위반했는지]

---

### ✅ Positive Observations

- [잘 따른 패턴이나 컨벤션]
- [칭찬할 만한 코드 품질]

---

### 💡 Improvement Suggestions (개선안)

#### 구조적 개선안:
1. [아키텍처 관점의 개선 제안]
2. [레이어 분리 개선]

#### 성능 개선안:
1. [성능 최적화 제안]

#### 유지보수성 개선안:
1. [코드 가독성 및 유지보수 개선]

---

## Usage Examples

```bash
/commit-review c11e0a4
/commit-review HEAD~3
/commit-review develop
```

---

## Review Process

1. **Collect changes**: Use git diff and git log to get changed files and commit history
2. **Read changed files**: Use Read tool to analyze modified files in detail
3. **Reference conventions**: Refer to @docs/convention/ documents for project rules
4. **Verify checklist**: Review all 10 categories above
5. **Provide comprehensive feedback**: Issues, positive observations, and improvement suggestions (in Korean)
