# Cherrish 개발 가이드

## 빠른 시작

```bash
# 1. 환경 설정 (처음 한 번만)
cp .env.example .env

# 2. DB 시작
docker-compose up -d

# 3. 앱 실행
./gradlew bootRun
```

**접속**: http://localhost:8080

---

## Docker 기본 명령어

### 시작/종료
```bash
docker-compose up -d              # DB 시작 (백그라운드)
docker-compose down               # DB 종료
docker-compose down -v            # DB 종료 + 데이터 삭제
```

### 모니터링
```bash
docker-compose ps                 # 실행 중인 컨테이너 확인
docker-compose logs -f            # 로그 실시간 확인
docker-compose logs -f postgres   # PostgreSQL 로그만 확인
```

### DB 접속
```bash
docker exec -it cherrish-postgres psql -U postgres -d cherrish

# psql 명령어
\dt                               # 테이블 목록
\d table_name                     # 테이블 구조
SELECT * FROM your_table;         # 쿼리 실행
\q                                # 종료
```

---

## 배포 가이드 (AWS EC2)

### 1. 이미지 빌드
```bash
./gradlew bootBuildImage
```

### 2. Docker Hub에 푸시
```bash
docker tag cherrish:0.0.1-SNAPSHOT your-dockerhub-username/cherrish:latest
docker login
docker push your-dockerhub-username/cherrish:latest
```

### 3. EC2에서 실행
```bash
# EC2 SSH 접속 후
docker pull your-dockerhub-username/cherrish:latest

docker run -d \
  --name cherrish-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-rds-endpoint \
  -e DB_PASSWORD=your-password \
  your-dockerhub-username/cherrish:latest
```

### 4. AWS RDS PostgreSQL 연결
- RDS 인스턴스 생성 (PostgreSQL 17)
- 보안 그룹: EC2에서 5432 포트 접근 허용
- 환경 변수로 RDS 엔드포인트 전달

---

## 참고

- **Gradle**: `./gradlew --help`
- **Spring Profiles**: `application-dev.yaml` (개발), `application-prod.yaml` (배포)
- **포트 충돌**: `.env`에서 `DB_PORT` 변경
