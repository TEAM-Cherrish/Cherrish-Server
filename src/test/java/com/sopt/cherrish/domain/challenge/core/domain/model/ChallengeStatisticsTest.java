package com.sopt.cherrish.domain.challenge.core.domain.model;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_CHALLENGE_TITLE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_HOMECARE_ROUTINE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_USER_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.FIXED_START_DATE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("ChallengeStatistics 도메인 단위 테스트")
class ChallengeStatisticsTest {

	private Challenge createTestChallenge() {
		return Challenge.builder()
			.userId(DEFAULT_USER_ID)
			.homecareRoutine(DEFAULT_HOMECARE_ROUTINE)
			.title(DEFAULT_CHALLENGE_TITLE)
			.startDate(FIXED_START_DATE)
			.build();
	}

	@Test
	@DisplayName("통계 초기화 - completedCount는 0")
	void initializeCompletedCountIsZero() {
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
	void incrementCompletedCountIncreasesCountByOne() {
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
	void decrementCompletedCountDecreasesCountByOne() {
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
	void decrementCompletedCountDoesNotGoBelowZero() {
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

	@ParameterizedTest
	@CsvSource({
		"0, 21, 0.0",      // 0%
		"10, 20, 50.0",    // 50%
		"21, 21, 100.0",   // 100%
		"0, 0, 0.0"        // totalCount=0
	})
	@DisplayName("진행률 계산")
	void getProgressPercentage(int completed, int total, double expected) {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(total)
			.build();

		statistics.adjustCompletedCount(completed);

		// when
		double percentage = statistics.getProgressPercentage();

		// then
		assertThat(percentage).isEqualTo(expected);
	}

	@Test
	@DisplayName("완료 개수 조정 - 양수 delta로 증가")
	void adjustCompletedCountPositiveDeltaIncreasesCount() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		// when
		statistics.adjustCompletedCount(5);

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(5);
	}

	@Test
	@DisplayName("완료 개수 조정 - 음수 delta로 감소")
	void adjustCompletedCountNegativeDeltaDecreasesCount() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		statistics.adjustCompletedCount(10);
		assertThat(statistics.getCompletedCount()).isEqualTo(10);

		// when
		statistics.adjustCompletedCount(-3);

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(7);
	}

	@Test
	@DisplayName("완료 개수 조정 - 음수 delta로 0 이하로 내려가지 않음")
	void adjustCompletedCountNegativeDeltaDoesNotGoBelowZero() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		statistics.adjustCompletedCount(3);
		assertThat(statistics.getCompletedCount()).isEqualTo(3);

		// when
		statistics.adjustCompletedCount(-5); // 3 - 5 = -2, but should be clamped to 0

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(0);
	}

	@Test
	@DisplayName("완료 개수 조정 - totalRoutineCount를 초과하지 않음")
	void adjustCompletedCountDoesNotExceedTotalRoutineCount() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		statistics.adjustCompletedCount(15);
		assertThat(statistics.getCompletedCount()).isEqualTo(15);

		// when
		statistics.adjustCompletedCount(10); // 15 + 10 = 25, but should be clamped to 21

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(21);
	}

	@Test
	@DisplayName("완료 개수 조정 - 오버플로우 방지 (큰 양수 delta)")
	void adjustCompletedCountPreventsOverflowWithLargePositiveDelta() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(100)
			.build();

		statistics.adjustCompletedCount(50);
		assertThat(statistics.getCompletedCount()).isEqualTo(50);

		// when
		statistics.adjustCompletedCount(Integer.MAX_VALUE); // int 오버플로우 발생 가능, but should be clamped to 100

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(100);
	}

	@Test
	@DisplayName("완료 개수 조정 - 오버플로우 방지 (큰 음수 delta)")
	void adjustCompletedCountPreventsOverflowWithLargeNegativeDelta() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(100)
			.build();

		statistics.adjustCompletedCount(50);
		assertThat(statistics.getCompletedCount()).isEqualTo(50);

		// when
		statistics.adjustCompletedCount(Integer.MIN_VALUE); // int 오버플로우 발생 가능, but should be clamped to 0

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(0);
	}

	@ParameterizedTest
	@CsvSource({
		"0, 0, 1",        // totalCount=0 → Level 1
		"0, 21, 1",       // 0% → Level 1
		"30, 100, 2",     // 30% → Level 2
		"60, 100, 3",     // 60% → Level 3
		"80, 100, 4"      // 80% → Level 4
	})
	@DisplayName("체리 레벨 계산")
	void calculateCherryLevel(int completed, int total, int expectedLevel) {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(total)
			.build();

		statistics.adjustCompletedCount(completed);

		// when
		int level = statistics.calculateCherryLevel();

		// then
		assertThat(level).isEqualTo(expectedLevel);
	}

	@Test
	@DisplayName("다음 레벨까지 진척도 - 총 루틴 개수가 0이면 0% 반환")
	void getProgressToNextLevelTotalCountZeroReturns0() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(0)
			.build();

		// when
		double progress = statistics.getProgressToNextLevel();

		// then
		assertThat(progress).isEqualTo(0.0);
	}

	@Test
	@DisplayName("다음 레벨까지 진척도 - 최대 레벨(4) 도달 시 100% 반환")
	void getProgressToNextLevelMaxLevelReturns100() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(100)
			.build();

		statistics.adjustCompletedCount(100); // 100%

		// when
		double progress = statistics.getProgressToNextLevel();

		// then
		assertThat(progress).isEqualTo(100.0);
	}

	@Test
	@DisplayName("다음 레벨까지 진척도 - 레벨 4에서 100% 미만일 때도 100% 반환")
	void getProgressToNextLevelLevel4Returns100() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(100)
			.build();

		statistics.adjustCompletedCount(80); // 80% → Level 4

		// when
		double progress = statistics.getProgressToNextLevel();

		// then
		assertThat(progress).isEqualTo(100.0);
	}

	@Test
	@DisplayName("총 루틴 개수 증가 - 양수 값으로 증가")
	void incrementTotalRoutineCountIncreasesTotalCount() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		// when
		statistics.incrementTotalRoutineCount(5);

		// then
		assertThat(statistics.getTotalRoutineCount()).isEqualTo(26);
	}

	@Test
	@DisplayName("총 루틴 개수 증가 - 0 이하 값은 무시")
	void incrementTotalRoutineCountIgnoresZeroOrNegative() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		// when
		statistics.incrementTotalRoutineCount(0);
		statistics.incrementTotalRoutineCount(-5);

		// then
		assertThat(statistics.getTotalRoutineCount()).isEqualTo(21); // 변화 없음
	}
}
