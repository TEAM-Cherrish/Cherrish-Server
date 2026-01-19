package com.sopt.cherrish.domain.challenge.core.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.global.config.JpaAuditConfig;
import com.sopt.cherrish.global.config.QueryDslConfig;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QueryDslConfig.class, JpaAuditConfig.class})
@DisplayName("ChallengeRepository 통합 테스트")
class ChallengeRepositoryTest {

	@Autowired
	private ChallengeRepository challengeRepository;

	private Challenge expiredActiveChallenge;
	private Challenge todayEndChallenge;
	private Challenge futureEndChallenge;
	private Challenge expiredInactiveChallenge;

	@BeforeEach
	void setUp() {
		// Given: 다양한 상태의 챌린지 생성
		LocalDate today = LocalDate.now();
		HomecareRoutine[] routines = HomecareRoutine.values();

		// 1. endDate가 어제인 활성 챌린지 (만료 대상)
		expiredActiveChallenge = Challenge.builder()
			.userId(1L)
			.homecareRoutine(routines[0])
			.title("만료된 활성 챌린지")
			.startDate(today.minusDays(10))
			.build();
		challengeRepository.save(expiredActiveChallenge);

		// 2. endDate가 오늘인 활성 챌린지 (만료 대상 아님)
		todayEndChallenge = Challenge.builder()
			.userId(2L)
			.homecareRoutine(routines[1 % routines.length])
			.title("오늘 종료되는 챌린지")
			.startDate(today.minusDays(6))
			.build();
		challengeRepository.save(todayEndChallenge);

		// 3. endDate가 내일인 활성 챌린지 (만료 대상 아님)
		futureEndChallenge = Challenge.builder()
			.userId(3L)
			.homecareRoutine(routines[2 % routines.length])
			.title("미래 종료 챌린지")
			.startDate(today.minusDays(5))
			.build();
		challengeRepository.save(futureEndChallenge);

		// 4. endDate가 어제이지만 이미 비활성화된 챌린지 (만료 대상 아님)
		expiredInactiveChallenge = Challenge.builder()
			.userId(4L)
			.homecareRoutine(routines[3 % routines.length])
			.title("이미 비활성화된 챌린지")
			.startDate(today.minusDays(10))
			.build();
		expiredInactiveChallenge.complete();
		challengeRepository.save(expiredInactiveChallenge);
	}

	@Test
	@DisplayName("만료된 활성 챌린지만 벌크 업데이트로 비활성화")
	void bulkUpdateExpiredChallengesSuccess() {
		// Given
		LocalDate today = LocalDate.now();

		// When: 벌크 업데이트 실행
		int updatedCount = challengeRepository.bulkUpdateExpiredChallenges(today);

		// Then: 1개만 업데이트 (endDate가 어제인 활성 챌린지)
		assertThat(updatedCount).isEqualTo(1);

		// 업데이트된 챌린지 확인
		Challenge updated = challengeRepository.findById(expiredActiveChallenge.getId()).orElseThrow();
		assertThat(updated.getIsActive()).isFalse();

		// 업데이트되지 않은 챌린지들 확인
		Challenge todayEnd = challengeRepository.findById(todayEndChallenge.getId()).orElseThrow();
		assertThat(todayEnd.getIsActive()).isTrue();

		Challenge futureEnd = challengeRepository.findById(futureEndChallenge.getId()).orElseThrow();
		assertThat(futureEnd.getIsActive()).isTrue();

		Challenge expiredInactive = challengeRepository.findById(expiredInactiveChallenge.getId()).orElseThrow();
		assertThat(expiredInactive.getIsActive()).isFalse(); // 여전히 false
	}

	@Test
	@DisplayName("만료된 챌린지가 없으면 업데이트 없음")
	void bulkUpdateExpiredChallengesNoExpiredChallenges() {
		// Given: 모든 챌린지가 만료되지 않은 상태로 설정
		LocalDate futureDate = LocalDate.now().minusDays(100); // 모든 챌린지보다 과거 날짜

		// When
		int updatedCount = challengeRepository.bulkUpdateExpiredChallenges(futureDate);

		// Then: 업데이트 없음
		assertThat(updatedCount).isEqualTo(0);
	}

	@Test
	@DisplayName("여러 만료된 챌린지를 한 번에 처리")
	void bulkUpdateExpiredChallengesMultipleExpired() {
		// Given: 추가로 만료된 챌린지 2개 생성
		LocalDate today = LocalDate.now();

		Challenge expired2 = Challenge.builder()
			.userId(5L)
			.homecareRoutine(HomecareRoutine.PORE_CARE)
			.title("만료된 챌린지 2")
			.startDate(today.minusDays(15))
			.build();
		challengeRepository.save(expired2);

		Challenge expired3 = Challenge.builder()
			.userId(6L)
			.homecareRoutine(HomecareRoutine.ELASTICITY_CARE)
			.title("만료된 챌린지 3")
			.startDate(today.minusDays(20))
			.build();
		challengeRepository.save(expired3);

		// When
		int updatedCount = challengeRepository.bulkUpdateExpiredChallenges(today);

		// Then: 총 3개 업데이트 (초기 1개 + 추가 2개)
		assertThat(updatedCount).isEqualTo(3);

		// 모두 비활성화 확인
		assertThat(challengeRepository.findById(expiredActiveChallenge.getId()).orElseThrow().getIsActive())
			.isFalse();
		assertThat(challengeRepository.findById(expired2.getId()).orElseThrow().getIsActive())
			.isFalse();
		assertThat(challengeRepository.findById(expired3.getId()).orElseThrow().getIsActive())
			.isFalse();
	}

}
