# CI/CD 설정 가이드

## 1. Discord Webhook 설정

### Discord Webhook URL 생성
1. Discord 서버에서 알림을 받을 채널 선택
2. 채널 설정 (톱니바퀴 아이콘) → **연동** → **웹후크**
3. **새 웹후크** 클릭
4. 웹후크 이름 설정 (예: "CI/CD Notifications")
5. **웹후크 URL 복사** 클릭

### GitHub Secrets에 Webhook URL 추가
1. GitHub 리포지토리 페이지로 이동
2. **Settings** → **Secrets and variables** → **Actions**
3. **New repository secret** 클릭
4. 다음 정보 입력:
   - Name: `DISCORD_WEBHOOK_URL`
   - Secret: 복사한 Discord Webhook URL 붙여넣기
5. **Add secret** 클릭

## 2. Branch Protection Rules 설정

### develop 브랜치 보호 설정
1. GitHub 리포지토리 → **Settings** → **Branches**
2. **Add branch protection rule** 클릭
3. Branch name pattern: `develop`
4. 다음 옵션 체크:
   - ✅ **Require a pull request before merging**
     - ✅ Require approvals (최소 1명)
   - ✅ **Require status checks to pass before merging**
     - ✅ Require branches to be up to date before merging
     - Status checks: `build` 선택 (첫 PR 생성 후 나타남)
   - ✅ **Do not allow bypassing the above settings**
5. **Create** 클릭

### main 브랜치 보호 설정
1. 위와 동일한 과정으로 `main` 브랜치에도 설정
2. Branch name pattern: `main`
3. 동일한 옵션 체크

## 3. CI 워크플로우 테스트

### 첫 PR 생성하여 테스트
1. 새 브랜치 생성:
   ```bash
   git checkout -b test/ci-setup
   ```

2. 변경사항 커밋:
   ```bash
   git add .
   git commit -m "chore: CI 파이프라인 설정"
   ```

3. GitHub에 푸시:
   ```bash
   git push -u origin test/ci-setup
   ```

4. GitHub에서 `develop` 브랜치로 PR 생성

5. CI 워크플로우 실행 확인:
   - GitHub Actions 탭에서 워크플로우 실행 확인
   - Discord 채널에서 알림 수신 확인

## 4. CI 파이프라인 단계

현재 구성된 CI 파이프라인은 다음 단계로 구성됩니다:

1. ✅ **코드 체크아웃**
2. ✅ **JDK 21 설정**
3. ✅ **Gradle 빌드**
4. ✅ **테스트 실행**
5. ✅ **Checkstyle 코드 스타일 체크**
6. ✅ **JaCoCo 테스트 커버리지 리포트 생성**
7. ✅ **커버리지 검증**
8. ✅ **Discord 알림** (성공/실패)

## 5. 트러블슈팅

### CI가 실패하는 경우
- Checkstyle 경고 확인: `./gradlew checkstyleMain checkstyleTest`
- 테스트 실패 확인: `./gradlew test`
- 로컬에서 전체 빌드 확인: `./gradlew clean build`

### Discord 알림이 오지 않는 경우
- GitHub Secrets에 `DISCORD_WEBHOOK_URL`이 올바르게 설정되었는지 확인
- Discord Webhook URL이 유효한지 확인

### Status Check가 보이지 않는 경우
- 최소 1번의 PR이 생성되어 CI가 실행되어야 Status Check 목록에 나타남
- 첫 PR 생성 후 Branch Protection Rule을 다시 편집하여 `build` 체크 추가
