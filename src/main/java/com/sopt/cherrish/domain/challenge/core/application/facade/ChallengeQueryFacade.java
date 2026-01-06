package com.sopt.cherrish.domain.challenge.core.application.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeStatisticsService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengeQueryFacade {

	private final ChallengeService challengeService;
	private final ChallengeRoutineService routineService;
	private final ChallengeStatisticsService statisticsService;
	private final Clock clock;

	/**
	 * 활성 챌린지 상세 조회
	 * @param userId 사용자 ID
	 * @return 챌린지 상세 응답
	 */
	@Transactional(readOnly = true)
	public ChallengeDetailResponseDto getActiveChallengeDetail(Long userId) {
		// 1. 활성 챌린지 조회
		Challenge challenge = challengeService.getActiveChallenge(userId);

		// 2. 오늘의 루틴 조회
		LocalDate today = LocalDate.now(clock);
		List<ChallengeRoutine> todayRoutines = routineService.getTodayRoutines(challenge.getId());

		// 3. 통계 조회
		ChallengeStatistics statistics = statisticsService.getStatistics(challenge.getId());

		// 4. 현재 일차 계산
		int currentDay = challenge.getCurrentDay(today);

		// 5. 응원 메시지 생성
		String cheeringMessage = currentDay + "일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!";

		// 6. 응답 DTO 생성
		return ChallengeDetailResponseDto.from(
			challenge,
			currentDay,
			statistics,
			todayRoutines,
			cheeringMessage
		);
	}
}
