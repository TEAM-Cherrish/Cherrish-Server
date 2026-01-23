# <img width="200" height="100" alt="logo" src="https://github.com/user-attachments/assets/7cde32c0-f76c-4cf0-b3bf-260befb360a1" />

<p align="center">
  <img width="800" height="400" alt="image" src="https://github.com/user-attachments/assets/7f0e0645-fdf2-43a7-ab38-86f08377e466" />
</p>

# <img src="https://github.com/user-attachments/assets/b7c69bb0-3579-4074-8eca-e7966128fffb" height="50"/> Cherrish 서비스 소개
> 미용 의료부터 관리 루틴까지, 개인의 추구미에 맞는 관리의 방향을 정리하고, 다운타임&일정 중복을 방지하는 뷰티 캘린더


<br/>

## <img src="https://github.com/user-attachments/assets/ac0bd614-bf51-4fde-9727-f6f3d70dafa2" height="40"/>Cherrish 주요 기능
- 피부 고민 키워드 기반 시술 리스트
- 시술 다운타임 설정 및 디데이 여유기간 시각화
- AI가 짜주는 챌린지 루틴 추천
- 챌린지 기반 체리 게이미피케이션


<br/>

## <img src="https://github.com/user-attachments/assets/ac0bd614-bf51-4fde-9727-f6f3d70dafa2" height="40"/>Tech Stack

| Category | Stack |
| --- | --- |
| **Language** | Java 21 (Temurin) |
| **Framework** | Spring Boot 3.5.9 |
| **ORM** | Spring Data JPA, QueryDSL 5.1.0 |
| **Database** | PostgreSQL 17, H2 (Test) |
| **AI** | Spring AI OpenAI 1.1.2 |
| **Documentation** | SpringDoc OpenAPI 2.8.14 |
| **Build** | Gradle, Jib 3.5.2 |
| **Infrastructure** | AWS (VPC, EC2, RDS, ECR, SSM) |
| **CI/CD** | GitHub Actions (OIDC) |
| **Web Server** | Nginx + Let's Encrypt SSL |

<br/>

### <img src="https://github.com/user-attachments/assets/ac0bd614-bf51-4fde-9727-f6f3d70dafa2" height="30"/> Tech Stack 소개 ###

**1️⃣ Framework: Spring Boot 3.5.9** <br/>
Spring Boot 4에 비해 충분히 검증된 안정화 버전이며, Spring AI와의 호환성 이슈가 없어 선택했습니다.
프로덕션 환경에서 검증된 버전을 사용하여 리스크를 최소화합니다.

**2️⃣ Architecture: DDD 레이어드 아키텍처** <br/>
도메인 중심의 설계로 비즈니스 로직과 기술적 관심사를 분리하여 유지보수성을 높였습니다.
Presentation - Application - Domain - Infrastructure 레이어로 구성됩니다.

**3️⃣ Database: PostgreSQL 17** <br/>
pgvector 등 벡터 검색 확장 기능으로 AI 챗봇 도입 시 유리하며, 복잡한 쿼리 처리 및 대용량 데이터 관리에 최적화되어 있습니다.

**4️⃣ AI Integration: Spring AI OpenAI** <br/>
Spring 생태계와 자연스럽게 통합되며, 다양한 AI 프로바이더로 쉽게 전환 가능한 추상화를 제공합니다.

**5️⃣ Container: Jib** <br/>
Dockerfile 없이 컨테이너 이미지를 빌드하며, 레이어 캐싱으로 빠른 증분 빌드를 지원합니다.
로컬 Docker 데몬 없이도 ECR에 직접 푸시가 가능합니다.

<br/>

## <img src="https://github.com/user-attachments/assets/ac0bd614-bf51-4fde-9727-f6f3d70dafa2" height="40"/>**Convention**

💫 [Git Workflow](conventions-Git-Workflow)<br/>
✍️ [Code Style Guide](conventions-CODE-STYLE)<br/>
📂 [Architecture Guide](architecture-Architecture)<br/>
🔌 [API Convention](conventions-API-Convention)<br/>
📝 [Coding Guide](conventions-Coding-Guide)<br/>

<br/>

## <img src="https://github.com/user-attachments/assets/ac0bd614-bf51-4fde-9727-f6f3d70dafa2" height="40"/>Contributors

| 🤴김규일<br/>[@Kimgyuilli](https://github.com/Kimgyuilli) | 🍒조서영<br/>[@ssyoung02](https://github.com/ssyoung02) |
| --- | --- |
| <img height="280" alt="profile-kimgyuill" src="https://github.com/user-attachments/assets/a528c521-79fd-4824-b7fc-fb2d353da875" /> | <img height="280" alt="profile-choseoyoung" src="https://github.com/user-attachments/assets/1f57fe03-66e4-48f4-ab00-7e2a0c5ed448" /> |

<br/>

## <img src="https://github.com/user-attachments/assets/ac0bd614-bf51-4fde-9727-f6f3d70dafa2" height="40"/>**Foldering**
```
📂 src/main/java/com.sopt.cherrish
┣ 📂 global                    # 공통 설정
┃ ┣ 📂 config                    # 전역 설정 (DB, Swagger 등)
┃ ┣ 📂 exception                 # 전역 예외 처리
┃ ┣ 📂 response                  # 공통 응답 형식
┃ ┣ 📂 entity                    # 공통 엔티티 (BaseEntity)
┃ ┗ 📂 annotation                # 커스텀 어노테이션
┣ 📂 domain                    # 도메인 영역
┃ ┣ 📂 user                      # 사용자 관리
┃ ┃ ┣ 📂 presentation              # Controller, DTO
┃ ┃ ┣ 📂 application               # Service, Facade
┃ ┃ ┣ 📂 domain                    # Entity, Repository
┃ ┃ ┗ 📂 exception                 # 도메인별 예외
┃ ┣ 📂 procedure                 # 시술 정보
┃ ┣ 📂 userprocedure             # 사용자 시술 일정
┃ ┣ 📂 challenge                 # 챌린지
┃ ┃ ┣ 📂 core                      # 핵심 챌린지 기능
┃ ┃ ┣ 📂 recommendation            # AI 추천 기능
┃ ┃ ┗ 📂 demo                      # 데모용 기능
┃ ┣ 📂 calendar                  # 캘린더
┃ ┗ 📂 maindashboard             # 메인 대시보드
┗ 📂 CherrishApplication.java
```

---

<p align="center">
  Made with by Cherrish Server Team
</p>
