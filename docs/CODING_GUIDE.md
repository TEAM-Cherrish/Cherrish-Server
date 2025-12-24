# 코딩 가이드

`Java - 21`

## 목차

- [1. DTO 작성 규칙](#1-dto-작성-규칙)
- [2. Entity 작성 규칙](#2-entity-작성-규칙)
- [3. Configuration 작성](#3-configuration-작성)
- [관련 문서](#관련-문서)

## 1. DTO 작성 규칙

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

## 2. Entity 작성 규칙

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

## 3. Configuration 작성

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

## 관련 문서

- [코드 스타일](./CODE_STYLE.md)
- [API 컨벤션](./API_CONVENTION.md)
- [Git 워크플로우](./GIT_WORKFLOW.md)
- [환경 설정 가이드](./SETUP_GUIDE.md)
