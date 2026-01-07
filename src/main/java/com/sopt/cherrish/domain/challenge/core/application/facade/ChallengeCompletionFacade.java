package com.sopt.cherrish.domain.challenge.core.application.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeStatisticsService;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengeCompletionFacade {

	private final ChallengeRoutineService routineService;
	private final ChallengeStatisticsService statisticsService;

	/**
	 * 루틴 완료 상태 토글 및 통계 업데이트
	 * @param userId 사용자 ID (소유자 검증용)
	 * @param routineId 루틴 ID
	 * @return 완료 응답
	 */
	@Transactional
	public RoutineCompletionResponseDto toggleRoutineCompletion(Long userId, Long routineId) {
		// 1. 루틴 조회
		ChallengeRoutine routine = routineService.getRoutineById(routineId);

		// 2. 소유자 검증 (도메인 로직)
		routine.getChallenge().validateOwner(userId);

		// 3. 완료 상태 토글
		routine.toggleCompletion();

		// 4. 통계 업데이트 (체리 레벨 자동 업데이트)
		Long challengeId = routine.getChallenge().getId();
		if (routine.getIsComplete()) {
			statisticsService.incrementCompletedCount(challengeId);
		} else {
			statisticsService.decrementCompletedCount(challengeId);
		}

		// 5. 응답 생성
		return RoutineCompletionResponseDto.from(routine);
	}
}
