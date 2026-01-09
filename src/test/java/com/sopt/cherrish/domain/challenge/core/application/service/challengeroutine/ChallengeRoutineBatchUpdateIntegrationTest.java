package com.sopt.cherrish.domain.challenge.core.application.service.challengeroutine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeStatisticsRepository;
import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeIntegrationTestFixture;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateItemRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineBatchUpdateResponseDto;
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
@DisplayName("ChallengeRoutineService 통합 테스트 - 루틴 일괄 업데이트")
class ChallengeRoutineBatchUpdateIntegrationTest {

	@Autowired
	private ChallengeRoutineService challengeRoutineService;
	@Autowired
	private ChallengeRoutineRepository routineRepository;
	@Autowired
	private ChallengeStatisticsRepository statisticsRepository;
	@Autowired
	private ChallengeIntegrationTestFixture fixture;

	private static final int ROUTINES_PER_DAY_MULTIPLE  = 3;

	@Nested
	@DisplayName("루틴 일괄 업데이트 - 기본 기능")
	class UpdateMultipleRoutinesBasicTests {

		@Test
		@DisplayName("성공 - 여러 루틴 일괄 완료 처리 (3개 모두 false → true)")
		void completeMultipleRoutinesSuccess() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINES_PER_DAY_MULTIPLE );
			List<ChallengeRoutine> routines = routineRepository.findByChallengeId(challenge.getId()).stream()
				.limit(3)
				.toList();

			// 초기 상태 확인
			routines.forEach(routine -> assertThat(routine.getIsComplete()).isFalse());

			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					routines.stream()
						.map(r -> new RoutineUpdateItemRequestDto(r.getId(), true))
						.toList()
				);

			// when
			RoutineBatchUpdateResponseDto response =
				challengeRoutineService.updateMultipleRoutines(user.getId(), request);

			// then
			assertSoftly(softly -> {
				softly.assertThat(response.updatedCount()).isEqualTo(3);
				softly.assertThat(response.routines()).hasSize(3);
				softly.assertThat(response.routines()).allMatch(ChallengeRoutineResponseDto::isComplete);

				// DB 상태 확인
				routines.forEach(routine -> {
					ChallengeRoutine updated = routineRepository.findById(routine.getId()).orElseThrow();
					softly.assertThat(updated.getIsComplete()).isTrue();
				});

				// 통계 확인
				ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId())
					.orElseThrow();
				softly.assertThat(stats.getCompletedCount()).isEqualTo(3);
			});
		}

		@Test
		@DisplayName("성공 - 여러 루틴 일괄 완료 취소 (3개 모두 true → false)")
		void cancelMultipleRoutinesSuccess() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINES_PER_DAY_MULTIPLE );
			List<ChallengeRoutine> routines = routineRepository.findByChallengeId(challenge.getId()).stream()
				.limit(3)
				.toList();

			// 먼저 모두 완료 처리
			routines.forEach(routine -> challengeRoutineService.toggleCompletion(user.getId(), routine.getId()));

			// 완료 상태 확인
			routines.forEach(routine -> {
				ChallengeRoutine completed = routineRepository.findById(routine.getId()).orElseThrow();
				assertThat(completed.getIsComplete()).isTrue();
			});

			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					routines.stream()
						.map(r -> new RoutineUpdateItemRequestDto(r.getId(), false))
						.toList()
				);

			// when
			RoutineBatchUpdateResponseDto response =
				challengeRoutineService.updateMultipleRoutines(user.getId(), request);

			// then
			assertSoftly(softly -> {
				softly.assertThat(response.updatedCount()).isEqualTo(3);
				softly.assertThat(response.routines()).hasSize(3);
				softly.assertThat(response.routines()).allMatch(r -> !r.isComplete());

				// DB 상태 확인
				routines.forEach(routine -> {
					ChallengeRoutine updated = routineRepository.findById(routine.getId()).orElseThrow();
					softly.assertThat(updated.getIsComplete()).isFalse();
				});

				// 통계 확인
				ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId())
					.orElseThrow();
				softly.assertThat(stats.getCompletedCount()).isEqualTo(0);
			});
		}

		@Test
		@DisplayName("성공 - 혼합 업데이트 (일부 완료, 일부 취소)")
		void mixedUpdateSuccess() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINES_PER_DAY_MULTIPLE );
			List<ChallengeRoutine> allRoutines = routineRepository.findByChallengeId(challenge.getId());

			// 첫 3개 루틴 중 2개를 완료 상태로 만들기
			challengeRoutineService.toggleCompletion(user.getId(), allRoutines.get(0).getId());
			challengeRoutineService.toggleCompletion(user.getId(), allRoutines.get(1).getId());

			// 현재 completedCount = 2
			ChallengeStatistics initialStats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertThat(initialStats.getCompletedCount()).isEqualTo(2);

			// 요청: 0번 취소(true→false), 1번 유지(true→true), 2번 완료(false→true)
			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					List.of(
						new RoutineUpdateItemRequestDto(allRoutines.get(0).getId(), false),  // 완료 → 미완료 (-1)
						new RoutineUpdateItemRequestDto(allRoutines.get(1).getId(), true),   // 완료 → 완료 (0)
						new RoutineUpdateItemRequestDto(allRoutines.get(2).getId(), true)    // 미완료 → 완료 (+1)
					)
				);

			// when
			RoutineBatchUpdateResponseDto response =
				challengeRoutineService.updateMultipleRoutines(user.getId(), request);

			// then - delta = -1 + 0 + 1 = 0, completedCount = 2
			assertSoftly(softly -> {
				softly.assertThat(response.updatedCount()).isEqualTo(3);

				ChallengeRoutine routine0 = routineRepository.findById(allRoutines.get(0).getId()).orElseThrow();
				ChallengeRoutine routine1 = routineRepository.findById(allRoutines.get(1).getId()).orElseThrow();
				ChallengeRoutine routine2 = routineRepository.findById(allRoutines.get(2).getId()).orElseThrow();

				softly.assertThat(routine0.getIsComplete()).isFalse();
				softly.assertThat(routine1.getIsComplete()).isTrue();
				softly.assertThat(routine2.getIsComplete()).isTrue();

				ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId())
					.orElseThrow();
				softly.assertThat(stats.getCompletedCount()).isEqualTo(2);
			});
		}

		@Test
		@DisplayName("성공 - 같은 상태로 업데이트 시 불필요한 토글 스킵")
		void skipUnnecessaryToggleWhenSameState() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINES_PER_DAY_MULTIPLE );
			List<ChallengeRoutine> routines = routineRepository.findByChallengeId(challenge.getId()).stream()
				.limit(3)
				.toList();

			// 초기 상태: 모두 false
			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					routines.stream()
						.map(r -> new RoutineUpdateItemRequestDto(r.getId(), false))  // false → false (변경 없음)
						.toList()
				);

			// when
			RoutineBatchUpdateResponseDto response =
				challengeRoutineService.updateMultipleRoutines(user.getId(), request);

			// then - 상태 변경 없음, completedCount = 0 유지
			assertSoftly(softly -> {
				softly.assertThat(response.updatedCount()).isEqualTo(3);
				softly.assertThat(response.routines()).allMatch(r -> !r.isComplete());

				ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId())
					.orElseThrow();
				softly.assertThat(stats.getCompletedCount()).isEqualTo(0);
			});
		}
	}

	@Nested
	@DisplayName("루틴 일괄 업데이트 - 통계 검증")
	class UpdateMultipleRoutinesStatisticsTests {

		@Test
		@DisplayName("성공 - completedCount 정확성 검증 (delta 기반)")
		void deltaBasedCompletedCountIsAccurate() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINES_PER_DAY_MULTIPLE ); // 21개 루틴
			List<ChallengeRoutine> allRoutines = routineRepository.findByChallengeId(challenge.getId());

			// 초기: 5개 완료 처리
			for (int i = 0; i < 5; i++) {
				challengeRoutineService.toggleCompletion(user.getId(), allRoutines.get(i).getId());
			}

			ChallengeStatistics initialStats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertThat(initialStats.getCompletedCount()).isEqualTo(5);

			// 요청: 2개 취소, 3개 추가 완료 → delta = +1
			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					List.of(
						new RoutineUpdateItemRequestDto(allRoutines.get(0).getId(), false),  // 완료 → 미완료 (-1)
						new RoutineUpdateItemRequestDto(allRoutines.get(1).getId(), false),  // 완료 → 미완료 (-1)
						new RoutineUpdateItemRequestDto(allRoutines.get(5).getId(), true),   // 미완료 → 완료 (+1)
						new RoutineUpdateItemRequestDto(allRoutines.get(6).getId(), true),   // 미완료 → 완료 (+1)
						new RoutineUpdateItemRequestDto(allRoutines.get(7).getId(), true)    // 미완료 → 완료 (+1)
					)
				);

			// when
			challengeRoutineService.updateMultipleRoutines(user.getId(), request);

			// then - completedCount = 5 - 2 + 3 = 6
			ChallengeStatistics finalStats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertThat(finalStats.getCompletedCount()).isEqualTo(6);
		}

		@Test
		@DisplayName("성공 - 체리 레벨 업데이트 검증 (여러 루틴 완료 → 레벨 상승)")
		void cherryLevelIncreasesWithBatchCompletion() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINES_PER_DAY_MULTIPLE ); // 21개 루틴
			List<ChallengeRoutine> allRoutines = routineRepository.findByChallengeId(challenge.getId());

			// 초기 레벨 확인
			ChallengeStatistics initialStats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertThat(initialStats.getCherryLevel()).isEqualTo(1);

			// 6개 완료 (28.5%) → 레벨 2
			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					allRoutines.stream()
						.limit(6)
						.map(r -> new RoutineUpdateItemRequestDto(r.getId(), true))
						.toList()
				);

			// when
			challengeRoutineService.updateMultipleRoutines(user.getId(), request);

			// then
			ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertSoftly(softly -> {
				softly.assertThat(stats.getCompletedCount()).isEqualTo(6);
				softly.assertThat(stats.getCherryLevel()).isEqualTo(2);
				softly.assertThat(stats.getProgressPercentage())
					.isCloseTo(28.5, org.assertj.core.data.Offset.offset(0.1));
			});
		}

		@Test
		@DisplayName("성공 - 완료 취소 시 레벨 하락 검증")
		void cherryLevelDecreasesWithBatchCancellation() {
			// given
			User user = fixture.createDefaultUser();
			Challenge challenge = fixture.createChallengeWithRoutines(user, ROUTINES_PER_DAY_MULTIPLE ); // 21개 루틴
			List<ChallengeRoutine> allRoutines = routineRepository.findByChallengeId(challenge.getId());

			// 16개 완료 (76.1%) → 레벨 4
			for (int i = 0; i < 16; i++) {
				challengeRoutineService.toggleCompletion(user.getId(), allRoutines.get(i).getId());
			}

			ChallengeStatistics initialStats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertThat(initialStats.getCherryLevel()).isEqualTo(4);

			// 6개 취소 (10개 남음 = 47.6%) → 레벨 2
			RoutineUpdateRequestDto request =
				new RoutineUpdateRequestDto(
					allRoutines.stream()
						.limit(6)
						.map(r -> new RoutineUpdateItemRequestDto(r.getId(), false))
						.toList()
				);

			// when
			challengeRoutineService.updateMultipleRoutines(user.getId(), request);

			// then
			ChallengeStatistics stats = statisticsRepository.findByChallengeId(challenge.getId())
				.orElseThrow();
			assertSoftly(softly -> {
				softly.assertThat(stats.getCompletedCount()).isEqualTo(10);
				softly.assertThat(stats.getCherryLevel()).isEqualTo(2);
				softly.assertThat(stats.getProgressPercentage())
					.isCloseTo(47.6, org.assertj.core.data.Offset.offset(0.1));
			});
		}
	}
}
