테스트 실행 결과 7개 중 3개의 테스트가 실패했습니다. NullPointerException이 발생하고 있네요. ChallengeCustomRoutineFacade 클래스를 확인해보겠습니다.

● Search(pattern: "**/ChallengeCustomRoutineFacade.java")
⎿  Found 1 file (ctrl+o to expand)

● Read(src\main\java\com\sopt\cherrish\domain\challenge\core\application\facade\ChallengeCustomRoutineFacade.java)
⎿  Read 67 lines

● 문제는 54번째 줄의 challenge.getStatistics()에서 null이 반환되는 것 같습니다. ChallengeService의 getActiveChallengeWithStatistics 메서드를 확인해보겠습니다.

● Search(pattern: "**/ChallengeService.java")
⎿  Found 1 file (ctrl+o to expand)

● Read(src\main\java\com\sopt\cherrish\domain\challenge\core\application\service\ChallengeService.java)
⎿  Read 89 lines

● 이제 ChallengeRepository의 findActiveChallengeWithStatistics 메서드를 확인해봐야 합니다.

● Search(pattern: "**/ChallengeRepository.java")
⎿  Found 1 file (ctrl+o to expand)

● Read(src\main\java\com\sopt\cherrish\domain\challenge\core\domain\repository\ChallengeRepository.java)
⎿  Read 37 lines

● 쿼리는 LEFT JOIN FETCH c.statistics를 사용하고 있네요. Challenge 엔티티에서 statistics 필드가 제대로 정의되어 있는지 확인해봐야 합니다.

● Search(pattern: "**/Challenge.java")
⎿  Found 1 file (ctrl+o to expand)

● Read(src\main\java\com\sopt\cherrish\domain\challenge\core\domain\model\Challenge.java)
⎿  Read 169 lines

● 문제를 찾았습니다. Challenge 엔티티에 statistics 필드가 있고, @OneToOne(mappedBy = "challenge")로 양방향 관계가 설정되어 있습니다.

테스트에서 Challenge와 ChallengeStatistics를 따로 저장하고 있는데, 양방향 관계의 연관관계가 제대로 설정되지 않았을 수 있습니다. ChallengeStatistics 엔티티를 확인해보겠습니다.

● Search(pattern: "**/ChallengeStatistics.java")
⎿  Found 1 file (ctrl+o to expand)

● Read(src\main\java\com\sopt\cherrish\domain\challenge\core\domain\model\ChallengeStatistics.java)
⎿  Read 173 lines

● 문제를 찾았습니다!

양방향 @OneToOne 관계에서:
- Challenge는 mappedBy로 관계의 주인이 아님
- ChallengeStatistics가 관계의 주인 (외래키를 가짐)

테스트에서 Challenge와 ChallengeStatistics를 따로 저장하면, 데이터베이스에는 정상적으로 저장되지만, JPA 영속성 컨텍스트의 1차 캐시에 있는 Challenge 엔티티의 statistics 필드는 여전히 null로 남아있습니.

이후 findActiveChallengeWithStatistics를 호출해도 1차 캐시에서 Challenge를 가져오기 때문에 statistics가 null인 상태로 반환됩니다.

테스트 코드에 EntityManager를 추가하여 영속성 컨텍스트를 비우도록 수정하겠습니다.
