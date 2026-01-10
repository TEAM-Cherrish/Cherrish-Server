package com.sopt.cherrish.domain.challenge.core.application.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.CustomRoutineAddRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.CustomRoutineAddResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengeCustomRoutineFacade {

	private final ChallengeService challengeService;
	private final ChallengeRoutineService routineService;
	private final Clock clock;

	/**
	 * 커스텀 루틴 추가 플로우 오케스트레이션
	 *
	 * 트랜잭션 경계: 이 메서드 전체
	 *
	 * @param userId 사용자 ID
	 * @param request 커스텀 루틴 추가 요청 DTO
	 * @return 커스텀 루틴 추가 응답 DTO
	 */
	@Transactional
	public CustomRoutineAddResponseDto addCustomRoutine(
		Long userId,
		CustomRoutineAddRequestDto request
	) {
		// 1. 활성 챌린지 조회 (통계와 함께)
		Challenge challenge = challengeService.getActiveChallengeWithStatistics(userId);

		// 2. 오늘 날짜 계산
		LocalDate today = LocalDate.now(clock);

		// 3. 커스텀 루틴 Batch Insert
		List<ChallengeRoutine> routines = routineService.createAndSaveCustomRoutine(
			challenge, request.routineName(), today
		);

		// 4. 통계 업데이트 (totalRoutineCount 증가, cherryLevel 재계산)
		ChallengeStatistics statistics = challenge.getStatistics();
		statistics.incrementTotalRoutineCount(routines.size());
		statistics.updateCherryLevel();

		// 5. Response DTO 변환
		return CustomRoutineAddResponseDto.from(
			challenge,
			request.routineName(),
			routines,
			statistics.getTotalRoutineCount()
		);
	}
}
