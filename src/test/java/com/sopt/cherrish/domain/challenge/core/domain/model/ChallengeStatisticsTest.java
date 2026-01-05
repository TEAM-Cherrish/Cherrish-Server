package com.sopt.cherrish.domain.challenge.core.domain.model;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

@DisplayName("ChallengeStatistics 도메인 단위 테스트")
class ChallengeStatisticsTest {

	private Challenge createTestChallenge() {
		return Challenge.builder()
			.userId(DEFAULT_USER_ID)
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title(DEFAULT_CHALLENGE_TITLE)
			.startDate(FIXED_START_DATE)
			.build();
	}

	@Test
	@DisplayName("통계 초기화 - completedCount는 0")
	void initialize_completedCountIsZero() {
		// given
		Challenge challenge = createTestChallenge();

		// when
		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(0);
		assertThat(statistics.getTotalRoutineCount()).isEqualTo(21);
	}

	@Test
	@DisplayName("완료 개수 증가")
	void incrementCompletedCount_increasesCountByOne() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		// when
		statistics.incrementCompletedCount();

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("완료 개수 감소")
	void decrementCompletedCount_decreasesCountByOne() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		statistics.incrementCompletedCount();
		statistics.incrementCompletedCount();
		assertThat(statistics.getCompletedCount()).isEqualTo(2);

		// when
		statistics.decrementCompletedCount();

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("완료 개수 감소 - 0 이하로 내려가지 않음")
	void decrementCompletedCount_doesNotGoBelowZero() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		assertThat(statistics.getCompletedCount()).isEqualTo(0);

		// when
		statistics.decrementCompletedCount();

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(0);
	}

	@Test
	@DisplayName("진행률 계산 - 0%")
	void getProgressPercentage_zeroCompleted_returns0() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		// when
		double percentage = statistics.getProgressPercentage();

		// then
		assertThat(percentage).isEqualTo(0.0);
	}

	@Test
	@DisplayName("진행률 계산 - 50%")
	void getProgressPercentage_halfCompleted_returns50() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(20)
			.build();

		for (int i = 0; i < 10; i++) {
			statistics.incrementCompletedCount();
		}

		// when
		double percentage = statistics.getProgressPercentage();

		// then
		assertThat(percentage).isEqualTo(50.0);
	}

	@Test
	@DisplayName("진행률 계산 - 100%")
	void getProgressPercentage_allCompleted_returns100() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		for (int i = 0; i < 21; i++) {
			statistics.incrementCompletedCount();
		}

		// when
		double percentage = statistics.getProgressPercentage();

		// then
		assertThat(percentage).isEqualTo(100.0);
	}

	@Test
	@DisplayName("진행률 계산 - 총 개수가 0인 경우 0% 반환")
	void getProgressPercentage_totalCountZero_returns0() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(0)
			.build();

		// when
		double percentage = statistics.getProgressPercentage();

		// then
		assertThat(percentage).isEqualTo(0.0);
	}
}
