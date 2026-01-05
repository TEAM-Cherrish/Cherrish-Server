package com.sopt.cherrish.domain.challenge.core.application.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeStatisticsService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengeCreationFacade {

	private final ChallengeService challengeService;
	private final ChallengeRoutineService routineService;
	private final ChallengeStatisticsService statisticsService;

	/**
	 * 챌린지 생성 플로우 오케스트레이션
	 *
	 * 트랜잭션 경계: 이 메서드 전체
	 *
	 * @param userId 사용자 ID
	 * @param request 챌린지 생성 요청 DTO
	 * @return 챌린지 생성 응답 DTO
	 */
	@Transactional
	public ChallengeCreateResponseDto createChallenge(
		Long userId, ChallengeCreateRequestDto request) {

		// 1. 활성 챌린지 중복 확인
		challengeService.validateNoDuplicateActiveChallenge(userId);

		// 2. HomecareRoutine 변환 및 유효성 검증
		HomecareRoutine routine = HomecareRoutine.fromId(request.homecareRoutineId());

		// 3. 챌린지 생성
		String title = "7일 챌린지"; // 고정값
		LocalDate startDate = LocalDate.now();

		Challenge challenge = challengeService.createChallenge(
			userId, routine, title, startDate);

		// 4. 챌린지 루틴 Batch Insert (routineNames × 7일)
		List<ChallengeRoutine> routines = routineService.createAndSaveRoutines(
			challenge, request.routineNames());

		// 5. 통계 초기화
		int totalRoutineCount = routines.size();
		statisticsService.initializeStatistics(challenge, totalRoutineCount);

		// 6. Response DTO 변환
		return ChallengeCreateResponseDto.from(challenge, routines, totalRoutineCount);
	}
}
