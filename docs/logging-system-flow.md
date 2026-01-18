# 로깅 시스템 구현 플로우

## 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        HTTP Request                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  1. MdcLoggingFilter (Ordered.HIGHEST_PRECEDENCE)               │
│     ├── requestId 생성 (서버 UUID)                               │
│     ├── MDC 설정: requestId, userId, method, uri, clientIp      │
│     └── 응답 헤더에 X-Request-Id 추가                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  2. HttpLoggingFilter                                           │
│     ├── 요청 시작 시간 기록                                       │
│     ├── 요청 처리                                                │
│     └── 완료 후 로그 출력 (method, uri, status, duration)        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  3. DispatcherServlet → Controller → Service                    │
│     └── 비즈니스 로직 (기존 코드)                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  4. GlobalExceptionHandler (예외 발생 시)                        │
│     └── ERROR 레벨 로그 출력                                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  5. Logback Appender                                            │
│     ├── dev: CONSOLE (컬러 텍스트)                               │
│     └── prod: JSON_CONSOLE + ASYNC_DISCORD (ERROR만)            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 구현 단계

### Step 1: 의존성 + Logback 기본 설정 ✅ 완료

| 파일 | 역할 |
|------|------|
| `build.gradle` | logstash-logback-encoder, logback-discord-appender 추가 |
| `logback-spring.xml` | 메인 설정, 프로필별 appender 분기 |
| `console-appender.xml` | dev: 텍스트, prod: JSON |
| `discord-appender.xml` | ERROR 레벨 Discord 웹훅 전송 |

---

### Step 2: MdcLoggingFilter 구현

**목적**: 모든 요청에 대해 추적 가능한 컨텍스트 설정

**흐름**:
```
요청 진입
    │
    ├── requestId 생성 (UUID 8자리)
    │
    ├── MDC에 설정
    │   ├── requestId
    │   ├── userId (X-User-Id 헤더에서 추출)
    │   ├── method (GET, POST 등)
    │   ├── uri (/api/users 등)
    │   └── clientIp (X-Forwarded-For 또는 RemoteAddr)
    │
    ├── 응답 헤더에 X-Request-Id 추가
    │
    ├── 다음 필터로 전달
    │
    └── finally: MDC.clear()
```

**생성 파일**:
- `global/logging/filter/MdcLoggingFilter.java`

---

### Step 3: HttpLoggingFilter 구현

**목적**: HTTP 요청/응답 정보 로깅

**흐름**:
```
요청 진입
    │
    ├── 시작 시간 기록
    │
    ├── 다음 필터로 전달 (비즈니스 로직 실행)
    │
    └── 완료 후
        ├── 응답 시간 계산
        ├── 상태 코드 확인
        └── 로그 출력
            ├── 2xx/3xx → INFO
            ├── 4xx → WARN
            └── 5xx → ERROR
```

**로그 포맷**:
```
INFO  - HTTP GET /api/users - 200 (45ms)
WARN  - HTTP POST /api/users - 400 (12ms)
ERROR - HTTP GET /api/users/1 - 500 (120ms)
```

**생성 파일**:
- `global/logging/filter/HttpLoggingFilter.java`

---

### Step 4: Discord 알림 연동

**목적**: prod 환경에서 ERROR 발생 시 즉시 알림

**흐름**:
```
ERROR 로그 발생
    │
    ▼
Logback AsyncAppender
    │
    ├── ThresholdFilter (ERROR 이상만)
    │
    └── DiscordAppender
        └── 웹훅으로 메시지 전송
```

**Discord 메시지 포맷**:
```
**[에러 로그]**
**시간**: 2026-01-18 14:30:25.123
**위치**: GlobalExceptionHandler.handleException:120
**RequestId**: abc12345
**UserId**: 1
**메시지**: Unexpected error occurred
```java.lang.NullPointerException...```
```

**설정 파일**:
- `discord-appender.xml` (이미 생성됨)
- `application-prod.yaml`에 웹훅 URL 추가 필요

---

## 환경별 동작

### dev/local/test 환경

```
[2026-01-18 14:30:25:1234] [http-nio-8080-exec-1] [abc12345] INFO  [HttpLoggingFilter.doFilter:45] - HTTP GET /api/users - 200 (45ms)
```

- 컬러 텍스트 로그
- DEBUG 레벨 활성화
- Hibernate SQL 출력

### prod 환경

```json
{
  "@timestamp": "2026-01-18T14:30:25.123+0900",
  "level": "INFO",
  "logger_name": "HttpLoggingFilter",
  "message": "HTTP GET /api/users - 200 (45ms)",
  "requestId": "abc12345",
  "userId": "1",
  "method": "GET",
  "uri": "/api/users",
  "clientIp": "192.168.1.1",
  "service": "cherrish"
}
```

- JSON 구조화 로그 (Loki 연동 대비)
- INFO 레벨
- ERROR 발생 시 Discord 알림

---

## 파일 구조

```
src/main/
├── java/com/sopt/cherrish/global/
│   └── logging/
│       └── filter/
│           ├── MdcLoggingFilter.java      ← Step 2
│           └── HttpLoggingFilter.java     ← Step 3
│
└── resources/
    ├── logback-spring.xml                 ✅ 완료
    ├── console-appender.xml               ✅ 완료
    ├── discord-appender.xml               ✅ 완료
    ├── application.yaml
    └── application-prod.yaml              ← Discord URL 추가
```

---

## 구현 순서 요약

| 순서 | 작업 | 상태 |
|:----:|------|:----:|
| 1 | 의존성 + Logback 설정 | ✅ |
| 2 | MdcLoggingFilter | ⬜ |
| 3 | HttpLoggingFilter | ⬜ |
| 4 | application-prod.yaml Discord URL | ⬜ |
| 5 | 빌드 및 테스트 | ⬜ |

---

## 향후 확장

```
현재 구현
    │
    ├─→ 비즈니스 로직 추적 필요 시
    │   └── AOP 로깅 추가 (@LogExecutionTime 등)
    │
    ├─→ 로그 검색/분석 필요 시
    │   └── Promtail + Loki + Grafana
    │
    └─→ MSA 전환 시
        └── Micrometer Tracing (traceId 전파)
```
