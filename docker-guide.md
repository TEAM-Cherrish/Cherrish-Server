🚀 Jib 빌드 및 실행 명령어

1️⃣ Jib로 Docker 이미지 빌드

# Windows (Git Bash)
./gradlew jibDockerBuild

# 또는 CMD/PowerShell
.\gradlew.bat jibDockerBuild

예상 시간: 첫 빌드 ~2-3분, 이후 증분 빌드 30초-1분

2️⃣ 전체 스택 실행

docker-compose up -d

3️⃣ 로그 확인

# 전체 로그
docker-compose logs -f

# 앱만
docker-compose logs -f app

# PostgreSQL만
docker-compose logs -f postgres

4️⃣ 상태 확인

# 컨테이너 상태
docker-compose ps

# 앱 헬스체크
curl http://localhost:8080

5️⃣ 종료

# 컨테이너 중지
docker-compose down

# 컨테이너 + 볼륨 삭제 (DB 데이터도 삭제)
docker-compose down -v

  ---
📝 개발 워크플로우

빠른 개발 (로컬 실행)

# 1. PostgreSQL만 Docker로 실행
docker-compose up -d postgres

# 2. 앱은 로컬에서 실행 (빠른 재시작)
./gradlew bootRun

통합 테스트 (Docker 환경)

# 1. Jib로 이미지 빌드 (30초-1분)
./gradlew jibDockerBuild

# 2. 전체 스택 실행
docker-compose up -d

# 3. 테스트 후 로그 확인
docker-compose logs -f app

코드 수정 후 재빌드

# 1. 이미지 재빌드 (증분 빌드로 빠름!)
./gradlew jibDockerBuild

# 2. 컨테이너 재시작
docker-compose restart app

  ---
⚡ Jib 추가 명령어

# Docker 없이 레지스트리로 직접 푸시 (CI/CD에서 유용)
./gradlew jib --image=registry.example.com/cherrish-app:v1.0.0

# 빌드만 하고 Docker에 로드하지 않음 (빌드 테스트)
./gradlew jibBuildTar

# Jib 설정 확인
./gradlew jibDockerBuild --info

  ---
🔧 트러블슈팅

이미지가 없다고 나올 때

# 먼저 Jib로 이미지 빌드
./gradlew jibDockerBuild

# 그 다음 docker-compose 실행
docker-compose up -d

포트 충돌 시

# 기존 컨테이너 확인
docker ps

# 충돌하는 컨테이너 중지
docker stop <container_id>

완전히 초기화하고 싶을 때

# 모든 컨테이너 및 볼륨 삭제
docker-compose down -v

# 이미지 삭제
docker rmi cherrish-app:latest postgres:17

# 처음부터 다시 시작
./gradlew jibDockerBuild
docker-compose up -d
