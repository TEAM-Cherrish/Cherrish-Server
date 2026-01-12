package com.sopt.cherrish.domain.challenge.demo.application.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.application.service.CheeringMessageGenerator;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeService;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeStatisticsService;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeRoutine;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeStatistics;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DemoChallengeAdvanceDayFacade {

	private final DemoChallengeService challengeService;
	private final DemoChallengeRoutineService routineService;
	private final DemoChallengeStatisticsService statisticsService;
	private final CheeringMessageGenerator cheeringMessageGenerator;

	/**
	 * 다음 날로 넘어가기 및 통계 재계산
	 * 1. 가상 날짜 +1일
	 * 2. 통계 재계산
	 * 3. 새 날짜의 루틴 조회 및 반환
	 */
	@Transactional
	public ChallengeDetailResponseDto advanceDay(Long userId, Long demoChallengeId) {
		// 1. 챌린지 조회 및 검증
		DemoChallenge challenge = challengeService.getActiveChallengeWithStatistics(userId);
		challenge.validateOwner(userId);

		// 2. 다음 날로 진행
		challenge.advanceDay();

		// 3. 통계 재계산
		statisticsService.recalculateStatistics(demoChallengeId);

		// 4. 새 날짜 기준으로 데이터 조회
		LocalDate currentDate = challenge.getCurrentVirtualDate();
		List<DemoChallengeRoutine> todayRoutines =
			routineService.getRoutinesByDate(demoChallengeId, currentDate);

		DemoChallengeStatistics statistics = challenge.getStatistics();
		int currentDay = challenge.getCurrentDay();
		String cheeringMessage =
			cheeringMessageGenerator.generate(currentDay, challenge.getTotalDays());

		// 5. 응답 반환
		return ChallengeDetailResponseDto.from(
			challenge,
			currentDay,
			statistics,
			todayRoutines,
			cheeringMessage
		);
	}
}
