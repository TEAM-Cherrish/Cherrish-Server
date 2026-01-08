package com.sopt.cherrish.domain.challenge.core.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeStatisticsRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeIntegrationTestFixture;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.global.config.QueryDslConfig;
import com.sopt.cherrish.global.config.TestClockConfig;
import com.sopt.cherrish.global.config.TestJpaAuditConfig;

@DataJpaTest
@Import({
	TestJpaAuditConfig.class,
	TestClockConfig.class,
	QueryDslConfig.class,
	ChallengeRoutineService.class,
	ChallengeIntegrationTestFixture.class
})
@DisplayName("ChallengeRoutineService 통합 테스트")
class ChallengeRoutineServiceIntegrationTest {

	@Autowired
	private ChallengeRoutineService challengeRoutineService;
	@Autowired
	private ChallengeRoutineRepository routineRepository;
	@Autowired
	private ChallengeStatisticsRepository statisticsRepository;
	@Autowired
	private ChallengeIntegrationTestFixture fixture;

	private static final int ROUTINE_COUNT_SMALL = 1;
	private static final int ROUTINE_COUNT_MEDIUM = 2;
	private static final int ROUTINE_COUNT_LARGE = 3;

	@Nested
	@DisplayName("루틴 완료/취소 토글 - 기본 기능")
	class ToggleCompletionBasicTests {

		@Test
		@DisplayName("성공 - 루틴 완료 처리 (false → true)")
		void completeRoutineSuccess() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_LARGE);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			assertThat(routine.getIsComplete()).isFalse();

			// when
			RoutineCompletionResponseDto response = challengeRoutineService.toggleCompletion(
				user.getId(), routine.getId());

			// then
			assertRoutineToggleResult(response, routine, challenge, true, 1, 1);
		}

		@Test
		@DisplayName("성공 - 루틴 완료 취소 (true → false)")
		void cancelRoutineSuccess() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_LARGE);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			// 루틴을 먼저 완료 상태로 만들기
			challengeRoutineService.toggleCompletion(user.getId(), routine.getId());

			ChallengeRoutine completedRoutine = routineRepository.findById(routine.getId()).orElseThrow();
			assertThat(completedRoutine.getIsComplete()).isTrue();

			// when - 완료 취소
			RoutineCompletionResponseDto response = challengeRoutineService.toggleCompletion(
				user.getId(), routine.getId());

			// then
			assertRoutineToggleResult(response, routine, challenge, false, 0, 1);
		}

		@Test
		@DisplayName("성공 - 같은 루틴을 여러 번 토글해도 데이터 일관성 유지")
		void repeatedTogglesMaintainDataConsistency() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_SMALL);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			// when & then - 10번 토글 반복
			for (int i = 0; i < 10; i++) {
				boolean expectedComplete = (i % 2 == 0);  // 짝수: 완료, 홀수: 취소

				RoutineCompletionResponseDto response = challengeRoutineService.toggleCompletion(
					user.getId(), routine.getId());

				assertSoftly(softly -> {
					softly.assertThat(response.isComplete()).isEqualTo(expectedComplete);

					ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId())
						.orElseThrow();
					softly.assertThat(stats.getCompletedCount()).isEqualTo(expectedComplete ? 1 : 0);
				});
			}
		}

		@Test
		@DisplayName("성공 - completedCount는 0 미만으로 내려가지 않음")
		void completedCountDoesNotDecrementBelowZero() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_SMALL);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			// 초기 상태 확인
			ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertThat(statistics.getCompletedCount()).isEqualTo(0);

			// when - 미완료 상태에서 토글 (완료 처리)
			challengeRoutineService.toggleCompletion(user.getId(), routine.getId());

			// then - completedCount가 1로 증가
			statistics = statisticsRepository.findByChallengeId(challenge.getId()).orElseThrow();
			assertThat(statistics.getCompletedCount()).isEqualTo(1);

			// when - 완료 상태에서 토글 (완료 취소)
			challengeRoutineService.toggleCompletion(user.getId(), routine.getId());

			// then - completedCount가 0으로 감소 (0 미만으로는 내려가지 않음)
			statistics = statisticsRepository.findByChallengeId(challenge.getId()).orElseThrow();
			assertThat(statistics.getCompletedCount()).isEqualTo(0);
		}
	}

	@Nested
	@DisplayName("체리 레벨 업데이트 - 진행률 기반")
	class CherryLevelUpdateTests {

		@Test
		@DisplayName("성공 - 여러 루틴 완료 시 체리 레벨 업데이트 (25%, 50%, 75%)")
		void multipleRoutineCompletionUpdatesLevels() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_MEDIUM); // 14개 루틴
			List<ChallengeRoutine> allRoutines = routineRepository.findAll();

			// when & then - 25% 도달 (14개 중 4개 완료) → 레벨 2
			completeRoutines(user, allRoutines, 0, 4);
			assertCherryLevel(challenge, 2, 4);

			// when & then - 50% 도달 (14개 중 7개 완료) → 레벨 3
			completeRoutines(user, allRoutines, 4, 7);
			assertCherryLevel(challenge, 3, 7);

			// when & then - 75% 도달 (14개 중 11개 완료) → 레벨 4
			completeRoutines(user, allRoutines, 7, 11);
			assertCherryLevel(challenge, 4, 11);
		}

		@Test
		@DisplayName("성공 - 체리 레벨 경계값 정확성 검증")
		void cherryLevelBoundaryValuesAreCorrect() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_LARGE); // 21개 루틴
			List<ChallengeRoutine> allRoutines = routineRepository.findAll();

			// Level 1: 0% ~ 24.99%
			// 5개 완료 = 23.8% → 레벨 1
			completeRoutines(user, allRoutines, 0, 5);
			assertCherryLevelWithProgress(challenge, 1, 5, 23.8);

			// Level 2: 25% ~ 49.99%
			// 6개 완료 = 28.5% → 레벨 2
			completeRoutines(user, allRoutines, 5, 6);
			assertCherryLevelWithProgress(challenge, 2, 6, 28.5);

			// 10개 완료 = 47.6% → 레벨 2
			completeRoutines(user, allRoutines, 6, 10);
			assertCherryLevelWithProgress(challenge, 2, 10, 47.6);

			// Level 3: 50% ~ 74.99%
			// 11개 완료 = 52.3% → 레벨 3
			completeRoutines(user, allRoutines, 10, 11);
			assertCherryLevelWithProgress(challenge, 3, 11, 52.3);

			// Level 4: 75% ~ 100%
			// 16개 완료 = 76.1% → 레벨 4
			completeRoutines(user, allRoutines, 11, 16);
			assertCherryLevelWithProgress(challenge, 4, 16, 76.1);

			// 21개 완료 = 100% → 레벨 4
			completeRoutines(user, allRoutines, 16, 21);
			assertCherryLevelWithProgress(challenge, 4, 21, 100.0);
		}
	}

	@Nested
	@DisplayName("예외 처리 - 실패 케이스")
	class ExceptionHandlingTests {

		@Test
		@DisplayName("실패 - 존재하지 않는 루틴")
		void throwsExceptionWhenRoutineNotFound() {
			// given
			User user = fixture.createDefaultUser();
			Long nonExistentRoutineId = 999L;

			// when & then
			assertThatThrownBy(() ->
				challengeRoutineService.toggleCompletion(user.getId(), nonExistentRoutineId))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.ROUTINE_NOT_FOUND);
		}

		@Test
		@DisplayName("실패 - 다른 사용자의 루틴 접근 (권한 없음)")
		void throwsExceptionWhenUnauthorizedUser() {
			// given
			User owner = fixture.createDefaultUser();
			User otherUser = fixture.createOtherUser();

			Challenge challenge = fixture.createChallengeWithRoutines(owner, ROUTINE_COUNT_LARGE);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			// when & then - 다른 사용자가 접근 시도
			assertThatThrownBy(() ->
				challengeRoutineService.toggleCompletion(otherUser.getId(), routine.getId()))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.UNAUTHORIZED_ACCESS);

			// then - 루틴과 통계가 변경되지 않았는지 확인
			ChallengeRoutine unchangedRoutine = routineRepository.findById(routine.getId()).orElseThrow();
			assertThat(unchangedRoutine.getIsComplete()).isFalse();

			ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertThat(statistics.getCompletedCount()).isEqualTo(0);
		}

		@Test
		@DisplayName("실패 - 통계가 없는 챌린지의 루틴은 조회되지 않음")
		void throwsExceptionWhenChallengeHasNoStatistics() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithoutStatistics(user);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			// when & then - INNER JOIN으로 인해 통계가 없으면 루틴 조회부터 실패
			assertThatThrownBy(() ->
				challengeRoutineService.toggleCompletion(user.getId(), routine.getId()))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.ROUTINE_NOT_FOUND);

			// then - 루틴 상태가 변경되지 않았음 (트랜잭션 롤백)
			ChallengeRoutine unchangedRoutine = routineRepository.findById(routine.getId()).orElseThrow();
			assertThat(unchangedRoutine.getIsComplete()).isFalse();
		}
	}

	@Nested
	@DisplayName("기간 외 루틴 수정 제한")
	class OutOfPeriodRestrictionTests {

		@Test
		@DisplayName("실패 - 챌린지 시작 전 루틴 수정 불가")
		void throwsExceptionWhenToggleBeforeStartDate() {
			// given: 내일 시작하는 챌린지 (startDate = 2024-01-02)
			User user = fixture.createDefaultUser();
			LocalDate futureStartDate = LocalDate.of(2024, 1, 2);
			Challenge challenge = fixture.createChallengeWithRoutines(
				user, ROUTINE_COUNT_SMALL, futureStartDate);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			// when & then: 현재(2024-01-01) 시점에 시작 전 루틴 수정 시도
			assertThatThrownBy(() ->
				challengeRoutineService.toggleCompletion(user.getId(), routine.getId()))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode",
					ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD);
		}

		@Test
		@DisplayName("실패 - 챌린지 종료 후 루틴 수정 불가")
		void throwsExceptionWhenToggleAfterEndDate() {
			// given: 과거에 종료된 챌린지 (2023-12-20 ~ 2023-12-26)
			User user = fixture.createDefaultUser();
			LocalDate pastStartDate = LocalDate.of(2023, 12, 20);
			Challenge challenge = fixture.createChallengeWithRoutines(
				user, ROUTINE_COUNT_SMALL, pastStartDate);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			// when & then: 현재(2024-01-01) 시점에 종료 후 루틴 수정 시도
			assertThatThrownBy(() ->
				challengeRoutineService.toggleCompletion(user.getId(), routine.getId()))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode",
					ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD);
		}

		@Test
		@DisplayName("성공 - 챌린지 첫날 루틴 수정 가능")
		void canToggleOnStartDate() {
			// given: 오늘 시작하는 챌린지 (startDate = 2024-01-01)
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_SMALL);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			// when: 첫날 루틴 수정
			RoutineCompletionResponseDto response = challengeRoutineService.toggleCompletion(
				user.getId(), routine.getId());

			// then: 정상 완료
			assertThat(response.isComplete()).isTrue();
		}

		@Test
		@DisplayName("성공 - 챌린지 마지막 날 루틴 수정 가능")
		void canToggleOnEndDate() {
			// given: 오늘 종료하는 챌린지 (2023-12-26 ~ 2024-01-01)
			User user = fixture.createDefaultUser();
			LocalDate startDate = LocalDate.of(2023, 12, 26);
			Challenge challenge = fixture.createChallengeWithRoutines(
				user, ROUTINE_COUNT_SMALL, startDate);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			assertThat(challenge.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 1)); // 검증

			// when: 마지막 날 루틴 수정
			RoutineCompletionResponseDto response = challengeRoutineService.toggleCompletion(
				user.getId(), routine.getId());

			// then: 정상 완료
			assertThat(response.isComplete()).isTrue();
		}

		@Test
		@DisplayName("성공 - 챌린지 기간 중 루틴 수정 가능")
		void canToggleDuringChallengePeriod() {
			// given: 진행 중인 챌린지 (2023-12-30 ~ 2024-01-05)
			User user = fixture.createDefaultUser();
			LocalDate startDate = LocalDate.of(2023, 12, 30);
			Challenge challenge = fixture.createChallengeWithRoutines(
				user, ROUTINE_COUNT_SMALL, startDate);
			ChallengeRoutine routine = routineRepository.findByChallengeId(challenge.getId()).getFirst();

			// when: 기간 내 루틴 수정
			RoutineCompletionResponseDto response = challengeRoutineService.toggleCompletion(
				user.getId(), routine.getId());

			// then: 정상 완료
			assertThat(response.isComplete()).isTrue();
		}
	}

	// Helper Methods
	private void assertRoutineToggleResult(RoutineCompletionResponseDto response, ChallengeRoutine routine,
		Challenge challenge, boolean expectedComplete, int expectedCompletedCount, int expectedLevel) {
		assertSoftly(softly -> {
			softly.assertThat(response.routineId()).isEqualTo(routine.getId());
			softly.assertThat(response.isComplete()).isEqualTo(expectedComplete);
			ChallengeRoutine updatedRoutine = routineRepository.findById(routine.getId()).orElseThrow();
			softly.assertThat(updatedRoutine.getIsComplete()).isEqualTo(expectedComplete);
			ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			softly.assertThat(statistics.getCompletedCount()).isEqualTo(expectedCompletedCount);
			softly.assertThat(statistics.getCherryLevel()).isEqualTo(expectedLevel);
		});
	}

	private void completeRoutines(User user, List<ChallengeRoutine> routines, int fromIndex, int toIndex) {
		for (int i = fromIndex; i < toIndex; i++) {
			challengeRoutineService.toggleCompletion(user.getId(), routines.get(i).getId());
		}
	}

	private void assertCherryLevel(Challenge challenge, int expectedLevel, int expectedCount) {
		ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId()).orElseThrow();
		assertSoftly(softly -> {
			softly.assertThat(stats.getCherryLevel()).isEqualTo(expectedLevel);
			softly.assertThat(stats.getCompletedCount()).isEqualTo(expectedCount);
		});
	}

	private void assertCherryLevelWithProgress(Challenge challenge, int expectedLevel, int expectedCount,
		double expectedProgress) {
		ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId()).orElseThrow();
		assertSoftly(softly -> {
			softly.assertThat(stats.getCherryLevel()).isEqualTo(expectedLevel);
			softly.assertThat(stats.getCompletedCount()).isEqualTo(expectedCount);
			softly.assertThat(stats.getProgressPercentage())
				.isCloseTo(expectedProgress, org.assertj.core.data.Offset.offset(0.1));
		});
	}
}
