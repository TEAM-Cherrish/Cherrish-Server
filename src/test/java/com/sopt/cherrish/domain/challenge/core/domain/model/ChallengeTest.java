package com.sopt.cherrish.domain.challenge.core.domain.model;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_CHALLENGE_TITLE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_USER_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.FIXED_START_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

@DisplayName("Challenge 도메인 단위 테스트")
class ChallengeTest {

	private Challenge createTestChallenge() {
		return Challenge.builder()
			.userId(DEFAULT_USER_ID)
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title(DEFAULT_CHALLENGE_TITLE)
			.startDate(FIXED_START_DATE)
			.build();
	}

	@Test
	@DisplayName("챌린지 생성 시 종료일은 시작일로부터 6일 후")
	void createChallengeEndDateIs6DaysAfterStartDate() {
		// given & when
		Challenge challenge = createTestChallenge();

		// then
		assertThat(challenge.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 7));
		assertThat(challenge.getTotalDays()).isEqualTo(7);
	}

	@Test
	@DisplayName("챌린지 루틴 생성 - 3개 루틴명 × 7일 = 21개 생성")
	void createChallengeRoutines3routinesCreates21routines() {
		// given
		Challenge challenge = createTestChallenge();
		List<String> routineNames = List.of("아침 세안", "토너 바르기", "크림 바르기");

		// when
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(routineNames);

		// then
		assertThat(routines).hasSize(21); // 3 × 7 = 21
	}

	@Test
	@DisplayName("챌린지 루틴 생성 - 첫날부터 마지막날까지 순차적으로 생성")
	void createChallengeRoutinesCreatesRoutinesSequentially() {
		// given
		Challenge challenge = createTestChallenge();
		List<String> routineNames = List.of("세안", "토너");

		// when
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(routineNames);

		// then
		// 첫날 루틴 (2개)
		assertThat(routines.get(0).getScheduledDate()).isEqualTo(LocalDate.of(2024, 1, 1));
		assertThat(routines.get(0).getName()).isEqualTo("세안");
		assertThat(routines.get(1).getScheduledDate()).isEqualTo(LocalDate.of(2024, 1, 1));
		assertThat(routines.get(1).getName()).isEqualTo("토너");

		// 마지막날 루틴 (2개)
		assertThat(routines.get(12).getScheduledDate()).isEqualTo(LocalDate.of(2024, 1, 7));
		assertThat(routines.get(13).getScheduledDate()).isEqualTo(LocalDate.of(2024, 1, 7));
	}

	@Test
	@DisplayName("챌린지 루틴 생성 - 각 루틴은 미완료 상태로 생성")
	void createChallengeRoutinesAllRoutinesAreIncomplete() {
		// given
		Challenge challenge = createTestChallenge();

		// when
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(List.of("세안"));

		// then
		assertThat(routines).allMatch(routine -> !routine.getIsComplete());
	}

	@Test
	@DisplayName("챌린지 루틴 생성 - 단일 루틴명 × 7일 = 7개 생성")
	void createChallengeRoutinesSingleRoutineCreates7routines() {
		// given
		Challenge challenge = createTestChallenge();

		// when
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(List.of("아침 보습"));

		// then
		assertThat(routines).hasSize(7);
	}

	@Test
	@DisplayName("챌린지 완료 - isActive가 false로 변경")
	void completeSetsIsActiveToFalse() {
		// given
		Challenge challenge = createTestChallenge();
		assertThat(challenge.getIsActive()).isTrue();

		// when
		challenge.complete();

		// then
		assertThat(challenge.getIsActive()).isFalse();
	}

	@Test
	@DisplayName("현재 일차 조회 - 챌린지 시작 전")
	void getCurrentDayBeforeStartReturnsZero() {
		// given
		Challenge challenge = createTestChallenge(); // startDate: 2024-01-01
		LocalDate beforeStart = LocalDate.of(2023, 12, 31);

		// when
		int currentDay = challenge.getCurrentDay(beforeStart);

		// then
		assertThat(currentDay).isEqualTo(0);
	}

	@Test
	@DisplayName("현재 일차 조회 - 챌린지 진행 중 (1일차)")
	void getCurrentDayFirstDayReturnsOne() {
		// given
		Challenge challenge = createTestChallenge(); // startDate: 2024-01-01
		LocalDate firstDay = LocalDate.of(2024, 1, 1);

		// when
		int currentDay = challenge.getCurrentDay(firstDay);

		// then
		assertThat(currentDay).isEqualTo(1);
	}

	@Test
	@DisplayName("현재 일차 조회 - 챌린지 진행 중 (3일차)")
	void getCurrentDayThirdDayReturnsThree() {
		// given
		Challenge challenge = createTestChallenge(); // startDate: 2024-01-01
		LocalDate thirdDay = LocalDate.of(2024, 1, 3);

		// when
		int currentDay = challenge.getCurrentDay(thirdDay);

		// then
		assertThat(currentDay).isEqualTo(3);
	}

	@Test
	@DisplayName("현재 일차 조회 - 챌린지 종료 후")
	void getCurrentDayAfterEndReturnsMaxDays() {
		// given
		Challenge challenge = createTestChallenge(); // endDate: 2024-01-07
		LocalDate afterEnd = LocalDate.of(2024, 1, 10);

		// when
		int currentDay = challenge.getCurrentDay(afterEnd);

		// then
		assertThat(currentDay).isEqualTo(7); // totalDays
	}

	@Test
	@DisplayName("커스텀 루틴 생성 - 오늘부터 종료일까지 생성")
	void createCustomRoutinesFromTodayCreatesRoutinesUntilEndDate() {
		// given
		Challenge challenge = createTestChallenge(); // startDate: 2024-01-01, endDate: 2024-01-07
		LocalDate today = LocalDate.of(2024, 1, 3); // 3일차

		// when
		List<ChallengeRoutine> routines = challenge.createCustomRoutinesFromToday("저녁 마사지", today);

		// then
		assertThat(routines).hasSize(5); // 3일차부터 7일차까지 = 5개
		assertThat(routines.get(0).getScheduledDate()).isEqualTo(LocalDate.of(2024, 1, 3));
		assertThat(routines.get(0).getName()).isEqualTo("저녁 마사지");
		assertThat(routines.get(4).getScheduledDate()).isEqualTo(LocalDate.of(2024, 1, 7));
	}

	@Test
	@DisplayName("커스텀 루틴 생성 - 마지막 날에 추가하면 1개만 생성")
	void createCustomRoutinesFromTodayOnLastDayCreatesOneRoutine() {
		// given
		Challenge challenge = createTestChallenge(); // endDate: 2024-01-07
		LocalDate lastDay = LocalDate.of(2024, 1, 7);

		// when
		List<ChallengeRoutine> routines = challenge.createCustomRoutinesFromToday("아침 스트레칭", lastDay);

		// then
		assertThat(routines).hasSize(1);
		assertThat(routines.get(0).getScheduledDate()).isEqualTo(lastDay);
	}

	@Test
	@DisplayName("커스텀 루틴 생성 - 모든 루틴은 미완료 상태로 생성")
	void createCustomRoutinesFromTodayAllRoutinesAreIncomplete() {
		// given
		Challenge challenge = createTestChallenge();
		LocalDate today = LocalDate.of(2024, 1, 1);

		// when
		List<ChallengeRoutine> routines = challenge.createCustomRoutinesFromToday("저녁 운동", today);

		// then
		assertThat(routines).allMatch(routine -> !routine.getIsComplete());
	}

	@Test
	@DisplayName("커스텀 루틴 생성 실패 - 챌린지 시작 전 날짜")
	void createCustomRoutinesFromTodayBeforeStartDateThrowsException() {
		// given
		Challenge challenge = createTestChallenge(); // startDate: 2024-01-01
		LocalDate beforeStart = LocalDate.of(2023, 12, 31);

		// when & then
		assertThat(catchThrowable(
			() -> challenge.createCustomRoutinesFromToday("루틴", beforeStart)
		))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode",
				ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD);
	}

	@Test
	@DisplayName("커스텀 루틴 생성 실패 - 챌린지 종료 후 날짜")
	void createCustomRoutinesFromTodayAfterEndDateThrowsException() {
		// given
		Challenge challenge = createTestChallenge(); // endDate: 2024-01-07
		LocalDate afterEnd = LocalDate.of(2024, 1, 8);

		// when & then
		assertThat(catchThrowable(
			() -> challenge.createCustomRoutinesFromToday("루틴", afterEnd)
		))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode",
				ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD);
	}
}
