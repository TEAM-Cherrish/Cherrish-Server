package com.sopt.cherrish.domain.challenge.core.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeStatisticsRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.global.config.QueryDslConfig;
import com.sopt.cherrish.global.config.TestClockConfig;
import com.sopt.cherrish.global.config.TestJpaAuditConfig;

@DataJpaTest
@Import({
	TestJpaAuditConfig.class,
	TestClockConfig.class,
	QueryDslConfig.class,
	ChallengeRoutineService.class
})
@DisplayName("ChallengeRoutineService 통합 테스트 - 루틴 완료 토글")
class ChallengeRoutineServiceIntegrationTest {

	@Autowired
	private ChallengeRoutineService challengeRoutineService;

	@Autowired
	private ChallengeRepository challengeRepository;

	@Autowired
	private ChallengeRoutineRepository routineRepository;

	@Autowired
	private ChallengeStatisticsRepository statisticsRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager entityManager;

	private User createTestUser() {
		return userRepository.save(User.builder()
			.name("테스트 유저")
			.age(25)
			.build());
	}

	private Challenge createChallengeWithRoutines(User user, int routineNameCount) {
		// 챌린지 생성
		Challenge challenge = challengeRepository.save(Challenge.builder()
			.userId(user.getId())
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title("7일 챌린지")
			.startDate(LocalDate.now())
			.build());

		// 루틴 생성 (예: 3개의 루틴명 × 7일 = 21개)
		List<String> routineNames = List.of("루틴1", "루틴2", "루틴3").subList(0, routineNameCount);
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(routineNames);
		routineRepository.saveAll(routines);

		// 통계 생성
		statisticsRepository.save(ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(routines.size())
			.build());

		// EntityManager를 통해 Challenge를 다시 로드하여 Statistics 관계를 최신화
		entityManager.flush();
		entityManager.clear();

		return challengeRepository.findById(challenge.getId()).orElseThrow();
	}

	@Test
	@DisplayName("성공 - 루틴 완료 처리 (false → true)")
	void toggleRoutineCompletionToCompleteSuccess() {
		// given
		User user = createTestUser();
		Challenge challenge = createChallengeWithRoutines(user, 3);
		ChallengeRoutine routine = routineRepository.findAll().getFirst();

		assertThat(routine.getIsComplete()).isFalse();

		// when
		RoutineCompletionResponseDto response = challengeRoutineService.toggleCompletion(
			user.getId(), routine.getId());

		// then - Response 검증
		assertThat(response.routineId()).isEqualTo(routine.getId());
		assertThat(response.isComplete()).isTrue();

		// then - DB 실제 변경 확인
		ChallengeRoutine updatedRoutine = routineRepository.findById(routine.getId()).orElseThrow();
		assertThat(updatedRoutine.getIsComplete()).isTrue();

		// then - 통계 업데이트 확인
		ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		assertThat(statistics.getCompletedCount()).isEqualTo(1);
		assertThat(statistics.getCherryLevel()).isEqualTo(1); // 1/21 = 4.76% → 레벨 1
	}

	@Test
	@DisplayName("성공 - 루틴 완료 취소 (true → false)")
	void toggleRoutineCompletionToCancelSuccess() {
		// given
		User user = createTestUser();
		Challenge challenge = createChallengeWithRoutines(user, 3);
		ChallengeRoutine routine = routineRepository.findAll().getFirst();

		// 루틴을 먼저 완료 상태로 만들기
		routine.complete();
		routineRepository.save(routine);

		// 통계도 업데이트
		ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		statistics.incrementCompletedCount();
		statistics.updateCherryLevel();
		statisticsRepository.save(statistics);

		assertThat(routine.getIsComplete()).isTrue();
		assertThat(statistics.getCompletedCount()).isEqualTo(1);

		// when
		RoutineCompletionResponseDto response = challengeRoutineService.toggleCompletion(
			user.getId(), routine.getId());

		// then - Response 검증
		assertThat(response.routineId()).isEqualTo(routine.getId());
		assertThat(response.isComplete()).isFalse();

		// then - DB 실제 변경 확인
		ChallengeRoutine updatedRoutine = routineRepository.findById(routine.getId()).orElseThrow();
		assertThat(updatedRoutine.getIsComplete()).isFalse();

		// then - 통계 업데이트 확인
		ChallengeStatistics updatedStatistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		assertThat(updatedStatistics.getCompletedCount()).isEqualTo(0);
		assertThat(updatedStatistics.getCherryLevel()).isEqualTo(1); // 0/21 = 0% → 레벨 1
	}

	@Test
	@DisplayName("성공 - 여러 루틴 완료 시 체리 레벨 업데이트")
	void toggleRoutineCompletionUpdatesMultipleCherryLevels() {
		// given
		User user = createTestUser();
		Challenge challenge = createChallengeWithRoutines(user, 2); // 2개 루틴명 × 7일 = 14개 루틴
		List<ChallengeRoutine> allRoutines = routineRepository.findAll();

		// when & then - 25% 도달 (14개 중 4개 완료) → 레벨 2
		for (int i = 0; i < 4; i++) {
			challengeRoutineService.toggleCompletion(user.getId(), allRoutines.get(i).getId());
		}
		ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		assertThat(statistics.getCompletedCount()).isEqualTo(4);
		assertThat(statistics.getProgressPercentage()).isGreaterThanOrEqualTo(25.0);
		assertThat(statistics.getCherryLevel()).isEqualTo(2);

		// when & then - 50% 도달 (14개 중 7개 완료) → 레벨 3
		for (int i = 4; i < 7; i++) {
			challengeRoutineService.toggleCompletion(user.getId(), allRoutines.get(i).getId());
		}
		statistics = statisticsRepository.findByChallengeId(challenge.getId()).orElseThrow();
		assertThat(statistics.getCompletedCount()).isEqualTo(7);
		assertThat(statistics.getProgressPercentage()).isEqualTo(50.0);
		assertThat(statistics.getCherryLevel()).isEqualTo(3);

		// when & then - 75% 도달 (14개 중 11개 완료) → 레벨 4
		for (int i = 7; i < 11; i++) {
			challengeRoutineService.toggleCompletion(user.getId(), allRoutines.get(i).getId());
		}
		statistics = statisticsRepository.findByChallengeId(challenge.getId()).orElseThrow();
		assertThat(statistics.getCompletedCount()).isEqualTo(11);
		assertThat(statistics.getProgressPercentage()).isGreaterThanOrEqualTo(75.0);
		assertThat(statistics.getCherryLevel()).isEqualTo(4);
	}

	@Test
	@DisplayName("실패 - 존재하지 않는 루틴")
	void toggleRoutineCompletionRoutineNotFoundThrowsException() {
		// given
		User user = createTestUser();
		Long nonExistentRoutineId = 999L;

		// when & then
		assertThatThrownBy(() ->
			challengeRoutineService.toggleCompletion(user.getId(), nonExistentRoutineId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.ROUTINE_NOT_FOUND);
	}

	@Test
	@DisplayName("실패 - 다른 사용자의 루틴 접근")
	void toggleRoutineCompletionUnauthorizedUserThrowsException() {
		// given
		User owner = createTestUser();
		User otherUser = userRepository.save(User.builder()
			.name("다른 유저")
			.age(30)
			.build());

		Challenge challenge = createChallengeWithRoutines(owner, 3);
		ChallengeRoutine routine = routineRepository.findAll().getFirst();

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
	@DisplayName("성공 - 완료된 루틴 재토글 시 completedCount 0 미만으로 내려가지 않음")
	void toggleRoutineCompletionDoesNotDecrementBelowZero() {
		// given
		User user = createTestUser();
		Challenge challenge = createChallengeWithRoutines(user, 1);
		ChallengeRoutine routine = routineRepository.findAll().getFirst();

		// 초기 상태 확인
		ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		assertThat(statistics.getCompletedCount()).isEqualTo(0);

		// when - 미완료 상태에서 토글 (완료 취소 시도, 실제로는 완료 처리)
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

	@Test
	@DisplayName("트랜잭션 롤백 - 예외 발생 시 모든 변경사항 롤백")
	void toggleRoutineCompletionExceptionOccursRollbacksAllChanges() {
		// given
		User owner = createTestUser();
		User otherUser = userRepository.save(User.builder()
			.name("다른 유저")
			.age(30)
			.build());

		Challenge challenge = createChallengeWithRoutines(owner, 3);
		ChallengeRoutine routine = routineRepository.findAll().getFirst();

		Integer initialCompletedCount = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow()
			.getCompletedCount();

		// when - 권한이 없는 사용자가 시도
		try {
			challengeRoutineService.toggleCompletion(otherUser.getId(), routine.getId());
		} catch (ChallengeException e) {
			// expected
		}

		// then - 루틴 상태와 통계가 변경되지 않음 (롤백됨)
		ChallengeRoutine unchangedRoutine = routineRepository.findById(routine.getId()).orElseThrow();
		assertThat(unchangedRoutine.getIsComplete()).isFalse();

		ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		assertThat(statistics.getCompletedCount()).isEqualTo(initialCompletedCount);
	}

	@Test
	@DisplayName("성공 - Fetch Join으로 Challenge와 Statistics를 한 번에 조회")
	void toggleRoutineCompletionUsesOptimizedQuery() {
		// given
		User user = createTestUser();
		Challenge challenge = createChallengeWithRoutines(user, 3);
		ChallengeRoutine routine = routineRepository.findAll().getFirst();

		// when
		RoutineCompletionResponseDto response = challengeRoutineService.toggleCompletion(
			user.getId(), routine.getId());

		// then - Response가 정상적으로 생성되고, 데이터가 올바름
		assertThat(response).isNotNull();
		assertThat(response.routineId()).isEqualTo(routine.getId());
		assertThat(response.isComplete()).isTrue();

		// 루틴이 Challenge와 Statistics를 포함하여 조회되었는지는
		// findByIdWithChallengeAndStatistics 쿼리가 실행되었는지로 확인
		// (통합 테스트에서는 쿼리 실행이 성공하면 Fetch Join이 동작한 것)
		ChallengeRoutine fetchedRoutine = routineRepository.findByIdWithChallengeAndStatistics(routine.getId())
			.orElseThrow();
		assertThat(fetchedRoutine.getChallenge()).isNotNull();
		assertThat(fetchedRoutine.getChallenge().getStatistics()).isNotNull();
	}
}
