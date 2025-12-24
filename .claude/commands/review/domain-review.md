---
description: 특정 도메인 또는 디렉토리의 코드를 종합적으로 리뷰합니다
argument-hint: <domain-name-or-path>
allowed-tools: Bash(git:*), Read, Grep, Glob
---

# Domain/Directory Code Review

Perform comprehensive review of code within a specific domain or directory.

## Context Collection

First, collect domain information:

!`git status`
!`git diff HEAD -- "src/main/java/com/sopt/cherrish/domain/$1/**"`

Find all Java files in the domain:
!`find "src/main/java/com/sopt/cherrish/domain/$1" -type f -name "*.java" 2>/dev/null || find "src/main/java/com/sopt/cherrish/$1" -type f -name "*.java" 2>/dev/null || echo "Domain not found"`

## Review Instructions

You are reviewing the **$1 domain** in a **Spring Boot 3.5.9 + Java 21 based DDD architecture** project.

**IMPORTANT: All review output must be written in Korean (한글).**

---

## Domain Structure Verification

### Expected DDD Structure

```
domain/$1/
├── presentation/          # Presentation Layer
│   ├── {Domain}Controller.java
│   └── dto/
│       ├── request/      # Request DTOs
│       └── response/     # Response DTOs
│
├── application/           # Application Layer
│   ├── service/          # Application Services
│   └── facade/           # Facades (cross-domain orchestration)
│
├── domain/                # Domain Layer
│   ├── model/            # Domain models
│   ├── repository/       # Repository interfaces
│   ├── service/          # Domain services
│   └── vo/               # Value Objects
│
└── infrastructure/        # Infrastructure Layer
    ├── persistence/
    │   ├── entity/       # JPA entities
    │   └── repository/   # Repository implementations
    └── external/         # External API integration
```

**First, understand the actual structure of this domain.**

---

## Review Categories

### 1. Domain Isolation

**Checklist**:
- [ ] Domain doesn't directly depend on other domains
- [ ] Cross-domain communication only through Facades
- [ ] No domain-specific code in `global/` package
- [ ] No circular dependencies

---

### 2. Layer Compliance

Verify each layer is in the correct package:

**Presentation Layer**:
- [ ] Controllers in `presentation/` package
- [ ] DTOs in `presentation/dto/{request, response}/`

**Application Layer**:
- [ ] Services in `application/service/`
- [ ] Facades in `application/facade/`

**Domain Layer**:
- [ ] Domain models in `domain/model/`
- [ ] Repository interfaces in `domain/repository/`

**Infrastructure Layer**:
- [ ] JPA Entities in `infrastructure/persistence/entity/`
- [ ] Repository implementations in `infrastructure/persistence/repository/`

---

### 3. Controller Layer Verification

Reference project conventions:
- @docs/convention/ARCHITECTURE.md
- @docs/convention/API_CONVENTION.md

**Controller pattern**:
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관련 API")
public class UserController {

    private final UserService userService;  // ✅ Only depends on Service

    @Operation(summary = "회원 조회")
    @GetMapping("/{id}")
    public CommonApiResponse<UserResponseDto> getUser(@PathVariable Long id) {
        return CommonApiResponse.success(SuccessCode.SUCCESS, userService.getUser(id));
    }
}
```

**Checklist**:
- [ ] Controller only calls Service (no direct Repository access)
- [ ] No business logic (delegate to Service)
- [ ] Only HTTP request/response handling
- [ ] Input validation with `@Valid`
- [ ] Returns `CommonApiResponse<T>`
- [ ] Swagger documentation with `@Tag`, `@Operation`

---

### 4. Service Layer Verification

**Service pattern**:
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // ✅ Class-level
public class UserService {

    private final UserRepository userRepository;

    // Read operations use readOnly
    public UserResponseDto getUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(user);  // ✅ DTO transformation
    }

    @Transactional  // ✅ Method-level for writes
    public UserResponseDto createUser(UserRequestDto request) {
        User user = request.toEntity();  // ✅ DTO → Entity
        User savedUser = userRepository.save(user);
        return UserResponseDto.from(savedUser);  // ✅ Entity → DTO
    }
}
```

**Checklist**:
- [ ] Class-level `@Transactional(readOnly = true)`
- [ ] Method-level `@Transactional` only for write operations
- [ ] DTO ↔ Entity transformation in Service
- [ ] Never return Entity directly (always return DTO)
- [ ] No HTTP-related code (no ResponseEntity, HttpStatus)
- [ ] Business validation logic included
- [ ] Orchestrates multiple Repositories for complex operations

---

### 5. Repository Layer Verification

**Repository pattern**:
```java
public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ JPA method naming
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByAgeGreaterThan(int age);

    // ✅ Complex queries use @Query
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% AND u.isActive = true")
    List<User> searchActiveUsers(@Param("keyword") String keyword);
}
```

**Checklist**:
- [ ] Extends `JpaRepository<Entity, ID>`
- [ ] Follows JPA method naming conventions (findBy, existsBy, deleteBy, countBy)
- [ ] Complex queries use `@Query` annotation
- [ ] No business logic (pure data access only)
- [ ] No DTO transformations (done in Service)

---

### 6. Entity Layer Verification

Reference: @docs/convention/CODING_GUIDE.md

**Entity pattern**:
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder
    private User(String email) {
        this.email = email;
    }

    // ✅ Business logic inside Entity
    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }
}
```

**Checklist**:
- [ ] No `@Setter` (immutability)
- [ ] Think twice before using `@Getter`
- [ ] Use `@Builder` for object creation
- [ ] Business logic as Entity methods
- [ ] Extends `BaseTimeEntity` (if createdAt, updatedAt needed)

---

### 7. DTO Pattern Verification

Reference: @docs/convention/CODING_GUIDE.md

**Request DTO**:
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRequestDto {

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    // ✅ DTO → Entity transformation
    public User toEntity() {
        return User.builder()
            .name(this.name)
            .email(this.email)
            .build();
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

    // ✅ Entity → DTO transformation
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
            .id(user.getId())
            .name(user.getName())
            .build();
    }
}
```

**Checklist**:
- [ ] Request DTO has `toEntity()` method
- [ ] Response DTO has `static from(Entity)` method
- [ ] Request: `@Getter` + `@NoArgsConstructor(access = PROTECTED)`
- [ ] Response: `@Getter` + `@Builder`
- [ ] Request DTO has validation annotations (@NotBlank, @Email, @Size, etc.)

---

### 8. API Response Pattern Verification

**All endpoints must use `CommonApiResponse<T>`**:

**Checklist**:
- [ ] All Controller methods return `CommonApiResponse<T>`
- [ ] Appropriate `SuccessCode` used
- [ ] Domain-specific `SuccessCode` enum extension (if needed)
- [ ] Domain-specific error definitions in `ErrorCode` enum

---

### 9. Exception Handling Verification

**Checklist**:
- [ ] Services throw `BaseException` + `ErrorCode`
- [ ] No generic Exception (RuntimeException, Exception)
- [ ] User-friendly error messages
- [ ] `GlobalExceptionHandler` handles all exceptions

---

### 10. Security Review

**SQL Injection**:
- [ ] No string concatenation in `@Query` (use parameter binding)
- [ ] Safe parameter binding for Native Queries

**Input Validation**:
- [ ] All Request DTOs have input validation
- [ ] Appropriate use of `@NotBlank`, `@Size`, `@Pattern`

**Sensitive Data**:
- [ ] No passwords/tokens exposed in logs
- [ ] Entities not exposed in Controllers (use DTOs)

**Authentication/Authorization**:
- [ ] Security annotations on protected endpoints (if needed)

---

### 11. Test Coverage Verification

Check test files for this domain:
!`find "src/test/java/com/sopt/cherrish/domain/$1" -type f -name "*.java" 2>/dev/null || echo "No test files found"`

**Checklist**:
- [ ] Unit tests exist for Service classes
- [ ] Integration tests for Controllers (if needed)
- [ ] Repository tests (for complex queries)
- [ ] Edge cases covered
- [ ] Test naming conventions followed

---

### 12. Code Quality

**Checkstyle validation**:
```bash
./gradlew checkstyleMain
```

**Checklist**:
- [ ] No wildcard imports
- [ ] Logging with `@Slf4j`
- [ ] Line length ≤ 140 chars
- [ ] Method length ≤ 150 lines
- [ ] File length ≤ 500 lines
- [ ] Parameters ≤ 7

---

### 13. Complexity Analysis

**Checklist**:
- [ ] Methods not too long (≤ 150 lines)
- [ ] Files not too long (≤ 500 lines)
- [ ] No excessive parameters (≤ 7)
- [ ] Clear separation of concerns
- [ ] No code duplication (common logic extractable)
- [ ] No unnecessary abstractions

---

## Output Format

**CRITICAL: Write all review results in Korean (한글).**

Structure your domain review as follows:

### 🏗️ Domain Overview: $1

- **도메인 목적**: [이 도메인이 무엇을 담당하는지]
- **존재하는 레이어**: [presentation / application / domain / infrastructure]
- **주요 엔티티**: [핵심 Entity 나열]
- **API 엔드포인트**: [엔드포인트 개수 및 간단 요약]
- **전체 파일 수**: X개
- **테스트 파일**: Y개

---

### 📐 Architectural Compliance

**레이어 분리**: [PASS / FAIL - 세부 내용]

**DDD 패턴**: [점수 또는 평가 및 설명]

**컨벤션 준수**: [백분율 또는 등급]

---

### ⚠️ Issues Found

For each issue:

#### 1. [카테고리] - [심각도] - 파일:라인

**문제점**:
[구체적 설명]

**현재 코드**:
```java
// 문제 코드
```

**권장 수정**:
```java
// 수정 코드
```

**근거**:
[왜 문제인지, 어떤 원칙 위반인지]

---

### ✅ Positive Observations

- [잘 구현된 부분]
- [모범 사례]

---

### 💡 Domain-Specific Recommendations

#### 도메인 아키텍처 개선:
1. [이 도메인의 구조적 개선 제안]
2. [레이어 분리 개선]

#### 리팩토링 기회:
1. [중복 제거나 개선 가능한 부분]

#### 도메인 모델 개선:
1. [Entity 설계 개선]
2. [Value Object 도입 제안]

---

## Usage Examples

```bash
/domain-review user
/domain-review post
/domain-review comment
/domain-review src/main/java/com/sopt/cherrish/global
```

---

## Review Process

1. **Explore domain files**: Use Glob/Grep to find all Java files in domain
2. **Analyze structure**: Verify actual layer structure matches DDD pattern
3. **Read files**: Use Read tool to examine Controller/Service/Repository/Entity/DTO
4. **Reference conventions**: Refer to @docs/convention/ documents
5. **Verify checklist**: Review all 13 categories above
6. **Provide domain-specific feedback**: Concrete improvement suggestions for this domain (in Korean)
