package com.sopt.cherrish.domain.challenge.demo.application.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeService;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeStatisticsService;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DemoChallengeAdvanceDayFacade {

	private final DemoChallengeService challengeService;
	private final DemoChallengeStatisticsService statisticsService;
	private final DemoChallengeQueryFacade queryFacade;

	/**
	 * 다음 날로 넘어가기 및 통계 재계산
	 * 1. 활성 데모 챌린지 조회
	 * 2. 가상 날짜 +1일 (종료일을 넘으면 챌린지 종료)
	 * 3. 통계 재계산
	 * 4. 새 날짜의 챌린지 상세 정보 조회 및 반환
	 */
	@Transactional(noRollbackFor = ChallengeException.class)
	public ChallengeDetailResponseDto advanceDay(Long userId) {
		// 1. 활성 챌린지 조회
		DemoChallenge challenge = challengeService.getActiveChallengeWithStatistics(userId);

		// 2. 다음 날로 진행 (종료일을 넘으면 isActive = false)
		challenge.advanceDay();

		// 3. 통계 재계산
		statisticsService.recalculateStatistics(challenge.getId());

		// 4. 챌린지 종료 시 예외 발생 (트랜잭션은 커밋됨)
		if (!challenge.getIsActive()) {
			throw new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND);
		}

		// 챌린지가 계속 진행 중인 경우
		return queryFacade.getActiveChallengeDetail(userId);
	}
}
