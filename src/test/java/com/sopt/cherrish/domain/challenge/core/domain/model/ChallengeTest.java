package com.sopt.cherrish.domain.challenge.core.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

@DisplayName("Challenge 도메인 단위 테스트")
class ChallengeTest {

	private static final LocalDate TEST_START_DATE = LocalDate.of(2024, 1, 1);
	private static final Long TEST_USER_ID = 1L;

	private Challenge createTestChallenge() {
		return Challenge.builder()
			.userId(TEST_USER_ID)
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title("7일 챌린지")
			.startDate(TEST_START_DATE)
			.build();
	}

	@Test
	@DisplayName("챌린지 생성 시 종료일은 시작일로부터 6일 후")
	void createChallenge_endDateIs6DaysAfterStartDate() {
		// given & when
		Challenge challenge = createTestChallenge();

		// then
		assertThat(challenge.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 7));
		assertThat(challenge.getTotalDays()).isEqualTo(7);
	}

	@Test
	@DisplayName("챌린지 루틴 생성 - 3개 루틴명 × 7일 = 21개 생성")
	void createChallengeRoutines_3routines_creates21routines() {
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
	void createChallengeRoutines_createsRoutinesSequentially() {
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
	void createChallengeRoutines_allRoutinesAreIncomplete() {
		// given
		Challenge challenge = createTestChallenge();

		// when
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(List.of("세안"));

		// then
		assertThat(routines).allMatch(routine -> !routine.getIsComplete());
	}

	@Test
	@DisplayName("챌린지 루틴 생성 - 단일 루틴명 × 7일 = 7개 생성")
	void createChallengeRoutines_singleRoutine_creates7routines() {
		// given
		Challenge challenge = createTestChallenge();

		// when
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(List.of("아침 보습"));

		// then
		assertThat(routines).hasSize(7);
	}

	@Test
	@DisplayName("챌린지 완료 - isActive가 false로 변경")
	void complete_setsIsActiveToFalse() {
		// given
		Challenge challenge = createTestChallenge();
		assertThat(challenge.getIsActive()).isTrue();

		// when
		challenge.complete();

		// then
		assertThat(challenge.getIsActive()).isFalse();
	}
}
