# 코드 스타일 가이드

`Java - 21`

## 목차

- [1. 네이밍 컨벤션](#1-네이밍-컨벤션)
- [2. 코드 스타일 가이드 (Checkstyle)](#2-코드-스타일-가이드-checkstyle)

## 1. 네이밍 컨벤션

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

## 2. 코드 스타일 가이드 (Checkstyle)

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

