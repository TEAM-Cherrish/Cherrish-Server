# Git 워크플로우

`Java - 21`

## 목차

- [1. Git Commit 컨벤션](#1-git-commit-컨벤션)
- [2. Git Branch 컨벤션](#2-git-branch-컨벤션)
- [3. Issue 잘 만들기](#3-issue-잘-만들기)
- [관련 문서](#관련-문서)

## 1. Git Commit 컨벤션

| 커밋 유형 | 의미 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `style` | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우 |
| `refactor` | 코드 리팩토링 |
| `test` | 테스트 코드, 리팩토링 테스트 코드 추가 |
| `chore` | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore |
| `rename` | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우 |
| `remove` | 파일을 삭제하는 작업만 수행한 경우 |
| `!HOTFIX` | 급하게 치명적인 버그를 고쳐야 하는 경우 |

```
feat (domain): 새로운 기능 추가
fix (domain): 버그 수정
refactor (domain): 코드 리팩토링
style (domain): 코드 포맷팅, 세미콜론 누락 등
docs: 문서 수정
test: 테스트 코드 추가/수정
chore: 빌드 업무, 패키지 매니저 수정
```

**예시**: feat (User): 로그인 기능 추가

## 2. Git Branch 컨벤션

```smalltalk
<이슈번호>-<커밋 유형>/내용

Git Flow

main(배포 버전 코드)
dev(개발 단계 코드)

// 영어로 쓰기
15-feature/implement-user-login
```

## 3. Issue 잘 만들기

```java
템플릿에 맞춰서

버그 찾으면 버그 이슈화
기능 추가 티켓 다 이슈화
```

## 관련 문서

- [코드 스타일](./CODE_STYLE.md)
- [코딩 가이드](./CODING_GUIDE.md)
- [API 컨벤션](./API_CONVENTION.md)
- [환경 설정 가이드](./SETUP_GUIDE.md)
