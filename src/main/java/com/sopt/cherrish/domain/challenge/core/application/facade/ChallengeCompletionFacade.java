package com.sopt.cherrish.domain.challenge.core.application.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengeCompletionFacade {

	private final ChallengeRoutineService routineService;

	/**
	 * 루틴 완료 상태 토글 및 통계 업데이트
	 *
	 * 쿼리 최적화:
	 * - Routine, Challenge, Statistics를 Fetch Join으로 한 번에 조회 (쿼리 1)
	 * - 이미 로드된 Statistics 객체를 직접 조작 (중복 조회 없음)
	 * - 총 3개 쿼리 (기존 4개 → 3개로 25% 감소)
	 *
	 * 동시성 제어:
	 * - ChallengeStatistics에 낙관적 락(@Version) 적용
	 * - 동시 수정 시 OptimisticLockingFailureException 발생
	 *
	 * @param userId 사용자 ID (소유자 검증용)
	 * @param routineId 루틴 ID
	 * @return 완료 응답
	 */
	@Transactional
	public RoutineCompletionResponseDto toggleRoutineCompletion(Long userId, Long routineId) {
		// 1. 루틴 조회 (Challenge와 Statistics를 함께 Fetch Join으로 로드)
		ChallengeRoutine routine = routineService.getRoutineByIdWithStatistics(routineId);

		// 2. 소유자 검증 (도메인 로직)
		routine.getChallenge().validateOwner(userId);

		// 3. 완료 상태 토글
		routine.toggleCompletion();

		// 4. 통계 업데이트
		ChallengeStatistics statistics = routine.getChallenge().getStatistics();
		if (routine.getIsComplete()) {
			statistics.incrementCompletedCount();
		} else {
			statistics.decrementCompletedCount();
		}
		statistics.updateCherryLevel();

		// 5. 응답 생성
		return RoutineCompletionResponseDto.from(routine);
	}
}
