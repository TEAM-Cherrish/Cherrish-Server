package com.sopt.cherrish.domain.challenge.core.domain.model;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_CHALLENGE_TITLE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_HOMECARE_ROUTINE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;

@DisplayName("ChallengeRoutine 도메인 단위 테스트")
class ChallengeRoutineTest {

	private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);

	private Challenge createTestChallenge() {
		return Challenge.builder()
			.userId(DEFAULT_USER_ID)
			.homecareRoutine(DEFAULT_HOMECARE_ROUTINE)
			.title(DEFAULT_CHALLENGE_TITLE)
			.startDate(TEST_DATE)
			.build();
	}

	@Nested
	@DisplayName("루틴 상태 변경")
	class RoutineStateTests {

		@Test
		@DisplayName("루틴 완료 - isComplete가 true로 변경")
		void completeSetsIsCompleteToTrue() {
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
	}

	@Nested
	@DisplayName("루틴 날짜 검증")
	class RoutineDateTests {

		@Test
		@DisplayName("특정 날짜에 예정된 루틴인지 확인 - 일치하는 경우")
		void isScheduledForMatchingDateReturnsTrue() {
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
		void isScheduledForDifferentDateReturnsFalse() {
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

	@Nested
	@DisplayName("루틴 생성 시 기간 검증")
	class RoutineCreationValidationTests {

		@Test
		@DisplayName("성공 - 챌린지 기간 내 루틴 생성")
		void canCreateRoutineWithinChallengePeriod() {
			// given
			Challenge challenge = createTestChallenge();
			// 챌린지 기간: 2024-01-15 ~ 2024-01-21 (7일)

			// when & then - 첫날
			ChallengeRoutine firstDay = ChallengeRoutine.builder()
				.challenge(challenge)
				.name("첫날 루틴")
				.scheduledDate(LocalDate.of(2024, 1, 15))
				.build();
			assertThat(firstDay).isNotNull();

			// when & then - 마지막 날
			ChallengeRoutine lastDay = ChallengeRoutine.builder()
				.challenge(challenge)
				.name("마지막 날 루틴")
				.scheduledDate(LocalDate.of(2024, 1, 21))
				.build();
			assertThat(lastDay).isNotNull();

			// when & then - 중간 날
			ChallengeRoutine middleDay = ChallengeRoutine.builder()
				.challenge(challenge)
				.name("중간 날 루틴")
				.scheduledDate(LocalDate.of(2024, 1, 18))
				.build();
			assertThat(middleDay).isNotNull();
		}

		@Test
		@DisplayName("실패 - 챌린지 시작 전 루틴 생성 불가")
		void cannotCreateRoutineBeforeStartDate() {
			// given
			Challenge challenge = createTestChallenge();
			// 챌린지 시작: 2024-01-15

			// when & then
			assertThatThrownBy(() ->
				ChallengeRoutine.builder()
					.challenge(challenge)
					.name("시작 전 루틴")
					.scheduledDate(LocalDate.of(2024, 1, 14))  // 하루 전
					.build())
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode",
					ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD);
		}

		@Test
		@DisplayName("실패 - 챌린지 종료 후 루틴 생성 불가")
		void cannotCreateRoutineAfterEndDate() {
			// given
			Challenge challenge = createTestChallenge();
			// 챌린지 종료: 2024-01-21 (시작일 + 6일)

			// when & then
			assertThatThrownBy(() ->
				ChallengeRoutine.builder()
					.challenge(challenge)
					.name("종료 후 루틴")
					.scheduledDate(LocalDate.of(2024, 1, 22))  // 하루 후
					.build())
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode",
					ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD);
		}
	}
}
