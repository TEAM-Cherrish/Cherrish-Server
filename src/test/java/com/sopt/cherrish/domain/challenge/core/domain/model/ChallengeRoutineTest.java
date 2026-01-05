package com.sopt.cherrish.domain.challenge.core.domain.model;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

@DisplayName("ChallengeRoutine 도메인 단위 테스트")
class ChallengeRoutineTest {

	private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);

	private Challenge createTestChallenge() {
		return Challenge.builder()
			.userId(DEFAULT_USER_ID)
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title(DEFAULT_CHALLENGE_TITLE)
			.startDate(TEST_DATE)
			.build();
	}

	@Test
	@DisplayName("루틴 완료 - isComplete가 true로 변경")
	void complete_setsIsCompleteToTrue() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeRoutine routine = ChallengeRoutine.builder()
			.challenge(challenge)
			.name("아침 세안")
			.scheduledDate(TEST_DATE)
			.build();

		assertThat(routine.getIsComplete()).isFalse();

		// when
		routine.complete();

		// then
		assertThat(routine.getIsComplete()).isTrue();
	}

	@Test
	@DisplayName("특정 날짜에 예정된 루틴인지 확인 - 일치하는 경우")
	void isScheduledFor_matchingDate_returnsTrue() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeRoutine routine = ChallengeRoutine.builder()
			.challenge(challenge)
			.name("아침 세안")
			.scheduledDate(TEST_DATE)
			.build();

		// when & then
		assertThat(routine.isScheduledFor(LocalDate.of(2024, 1, 15))).isTrue();
	}

	@Test
	@DisplayName("특정 날짜에 예정된 루틴인지 확인 - 일치하지 않는 경우")
	void isScheduledFor_differentDate_returnsFalse() {
		// given
		Challenge challenge = createTestChallenge();

		ChallengeRoutine routine = ChallengeRoutine.builder()
			.challenge(challenge)
			.name("아침 세안")
			.scheduledDate(TEST_DATE)
			.build();

		// when & then
		assertThat(routine.isScheduledFor(LocalDate.of(2024, 1, 16))).isFalse();
	}
}
