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
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateItemDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateRequestDto;
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
@DisplayName("ChallengeRoutineService 통합 테스트 - 루틴 일괄 업데이트 (예외/트랜잭션)")
class ChallengeRoutineBatchUpdateExceptionTest {

	@Autowired
	private ChallengeRoutineService challengeRoutineService;
	@Autowired
	private ChallengeRoutineRepository routineRepository;
	@Autowired
	private ChallengeStatisticsRepository statisticsRepository;
	@Autowired
	private ChallengeIntegrationTestFixture fixture;

	private static final int ROUTINE_COUNT_SMALL = 1;
	private static final int ROUTINE_COUNT_LARGE = 3;

	@Nested
	@DisplayName("루틴 일괄 업데이트 - 예외 처리")
	class UpdateMultipleRoutinesExceptionTests {

		@Test
		@DisplayName("실패 - 일부 루틴 존재하지 않음 (전체 롤백)")
		void throwsExceptionWhenSomeRoutinesNotFound() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_LARGE);
			List<ChallengeRoutine> routines = routineRepository.findByChallengeId(challenge.getId()).stream()
				.limit(2)
				.toList();

			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					List.of(
						new RoutineUpdateItemDto(routines.getFirst().getId(), true),
						new RoutineUpdateItemDto(999L, true)  // 존재하지 않는 ID
					)
				);

			// when & then
			assertThatThrownBy(() ->
				challengeRoutineService.updateMultipleRoutines(user.getId(), request))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.ROUTINE_NOT_FOUND);

			// 전체 롤백 확인
			ChallengeRoutine unchanged = routineRepository.findById(routines.getFirst().getId()).orElseThrow();
			assertThat(unchanged.getIsComplete()).isFalse();

			ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertThat(stats.getCompletedCount()).isEqualTo(0);
		}

		@Test
		@DisplayName("실패 - 서로 다른 챌린지의 루틴 (ROUTINES_FROM_DIFFERENT_CHALLENGES)")
		void throwsExceptionWhenRoutinesFromDifferentChallenges() {
			// given - 각 챌린지마다 다른 사용자 사용 (활성 챌린지는 사용자당 1개만 가능)
			User user1 = fixture.createDefaultUser();
			User user2 = fixture.createOtherUser();
			Challenge challenge1 = fixture.createChallengeWithRoutines(user1, ROUTINE_COUNT_SMALL);
			Challenge challenge2 = fixture.createChallengeWithRoutines(user2, ROUTINE_COUNT_SMALL);

			ChallengeRoutine routine1 = routineRepository.findByChallengeId(challenge1.getId()).getFirst();
			ChallengeRoutine routine2 = routineRepository.findByChallengeId(challenge2.getId()).getFirst();

			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					List.of(
						new RoutineUpdateItemDto(routine1.getId(), true),
						new RoutineUpdateItemDto(routine2.getId(), true)
					)
				);

			// when & then - user1이 요청
			assertThatThrownBy(() ->
				challengeRoutineService.updateMultipleRoutines(user1.getId(), request))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode",
					ChallengeErrorCode.ROUTINES_FROM_DIFFERENT_CHALLENGES);

			// 롤백 확인
			ChallengeRoutine unchanged1 = routineRepository.findById(routine1.getId()).orElseThrow();
			ChallengeRoutine unchanged2 = routineRepository.findById(routine2.getId()).orElseThrow();
			assertThat(unchanged1.getIsComplete()).isFalse();
			assertThat(unchanged2.getIsComplete()).isFalse();
		}

		@Test
		@DisplayName("실패 - 권한 없는 사용자 (UNAUTHORIZED_ACCESS)")
		void throwsExceptionWhenUnauthorizedUser() {
			// given
			User owner = fixture.createDefaultUser();
			User otherUser = fixture.createOtherUser();

			Challenge challenge = fixture.createChallengeWithRoutines(owner, ROUTINE_COUNT_LARGE);
			List<ChallengeRoutine> routines = routineRepository.findByChallengeId(challenge.getId()).stream()
				.limit(3)
				.toList();

			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					routines.stream()
						.map(r -> new RoutineUpdateItemDto(r.getId(), true))
						.toList()
				);

			// when & then
			assertThatThrownBy(() ->
				challengeRoutineService.updateMultipleRoutines(otherUser.getId(), request))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.UNAUTHORIZED_ACCESS);

			// 롤백 확인
			routines.forEach(routine -> {
				ChallengeRoutine unchanged = routineRepository.findById(routine.getId()).orElseThrow();
				assertThat(unchanged.getIsComplete()).isFalse();
			});

			ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertThat(stats.getCompletedCount()).isEqualTo(0);
		}

		@Test
		@DisplayName("실패 - 챌린지 기간 외 루틴 (ROUTINE_OUT_OF_CHALLENGE_PERIOD)")
		void throwsExceptionWhenOutOfChallengePeriod() {
			// given: 과거에 종료된 챌린지 (2023-12-20 ~ 2023-12-26)
			User user = fixture.createDefaultUser();
			LocalDate pastStartDate = LocalDate.of(2023, 12, 20);
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_SMALL, pastStartDate);

			List<ChallengeRoutine> routines = routineRepository.findByChallengeId(challenge.getId()).stream()
				.limit(3)
				.toList();

			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					routines.stream()
						.map(r -> new RoutineUpdateItemDto(r.getId(), true))
						.toList()
				);

			// when & then: 현재(2024-01-01) 시점에 종료 후 루틴 수정 시도
			assertThatThrownBy(() ->
				challengeRoutineService.updateMultipleRoutines(user.getId(), request))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode",
					ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD);
		}
	}

	@Nested
	@DisplayName("루틴 일괄 업데이트 - 트랜잭션 검증")
	class UpdateMultipleRoutinesTransactionTests {

		@Test
		@DisplayName("검증 - All or Nothing 동작 (실패 시 전체 롤백)")
		void allOrNothingRollbackOnFailure() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINE_COUNT_LARGE);
			List<ChallengeRoutine> routines = routineRepository.findByChallengeId(challenge.getId()).stream()
				.limit(3)
				.toList();

			// 초기 통계 확인
			ChallengeStatistics initialStats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			int initialCompletedCount = initialStats.getCompletedCount();
			int initialLevel = initialStats.getCherryLevel();

			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					List.of(
						new RoutineUpdateItemDto(routines.get(0).getId(), true),
						new RoutineUpdateItemDto(routines.get(1).getId(), true),
						new RoutineUpdateItemDto(999L, true)  // 존재하지 않는 ID → 예외 발생
					)
				);

			// when & then
			assertThatThrownBy(() ->
				challengeRoutineService.updateMultipleRoutines(user.getId(), request))
				.isInstanceOf(ChallengeException.class);

			// 전체 롤백 확인: 모든 루틴이 변경되지 않아야 함
			routines.forEach(routine -> {
				ChallengeRoutine unchanged = routineRepository.findById(routine.getId()).orElseThrow();
				assertThat(unchanged.getIsComplete()).isFalse();
			});

			// 통계도 변경되지 않아야 함
			ChallengeStatistics finalStats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertSoftly(softly -> {
				softly.assertThat(finalStats.getCompletedCount()).isEqualTo(initialCompletedCount);
				softly.assertThat(finalStats.getCherryLevel()).isEqualTo(initialLevel);
			});
		}

		@Test
		@DisplayName("검증 - 예외 발생 시 통계 변경 없음")
		void statisticsUnchangedOnException() {
			// given
			User owner = fixture.createDefaultUser();
			User otherUser = fixture.createOtherUser();

			Challenge challenge = fixture.createChallengeWithRoutines(owner, ROUTINE_COUNT_LARGE);
			List<ChallengeRoutine> routines = routineRepository.findByChallengeId(challenge.getId()).stream()
				.limit(5)
				.toList();

			// 일부 루틴을 완료 처리하여 통계 생성
			challengeRoutineService.toggleCompletion(owner.getId(), routines.get(0).getId());
			challengeRoutineService.toggleCompletion(owner.getId(), routines.get(1).getId());

			ChallengeStatistics beforeStats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			int beforeCompletedCount = beforeStats.getCompletedCount();
			int beforeLevel = beforeStats.getCherryLevel();
			Long beforeVersion = beforeStats.getVersion();

			assertThat(beforeCompletedCount).isEqualTo(2);

			// 권한 없는 사용자가 일괄 업데이트 시도
			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					routines.stream()
						.skip(2)
						.limit(3)
						.map(r -> new RoutineUpdateItemDto(r.getId(), true))
						.toList()
				);

			// when & then
			assertThatThrownBy(() ->
				challengeRoutineService.updateMultipleRoutines(otherUser.getId(), request))
				.isInstanceOf(ChallengeException.class);

			// 통계가 변경되지 않았는지 확인
			ChallengeStatistics afterStats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertSoftly(softly -> {
				softly.assertThat(afterStats.getCompletedCount()).isEqualTo(beforeCompletedCount);
				softly.assertThat(afterStats.getCherryLevel()).isEqualTo(beforeLevel);
				softly.assertThat(afterStats.getVersion()).isEqualTo(beforeVersion);
			});
		}
	}
}
