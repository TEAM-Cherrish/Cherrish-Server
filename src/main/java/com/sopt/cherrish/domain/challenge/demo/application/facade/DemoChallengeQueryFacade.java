package com.sopt.cherrish.domain.challenge.demo.application.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeService;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeRoutine;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeStatistics;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DemoChallengeQueryFacade {

	private final DemoChallengeService challengeService;
	private final DemoChallengeRoutineService routineService;

	/**
	 * 활성 데모 챌린지 상세 조회
	 */
	@Transactional(readOnly = true)
	public ChallengeDetailResponseDto getActiveChallengeDetail(Long userId) {
		DemoChallenge challenge = challengeService.getActiveChallengeWithStatistics(userId);
		return buildChallengeDetailResponse(challenge);
	}

	/**
	 * 챌린지 상세 응답 DTO 생성
	 */
	public ChallengeDetailResponseDto buildChallengeDetailResponse(DemoChallenge challenge) {
		LocalDate currentDate = challenge.getCurrentVirtualDate();
		List<DemoChallengeRoutine> todayRoutines = routineService.getRoutinesByDate(challenge.getId(), currentDate);
		DemoChallengeStatistics statistics = challenge.getStatistics();
		int currentDay = challenge.getCurrentDay();

		return ChallengeDetailResponseDto.from(
			challenge,
			currentDay,
			statistics,
			todayRoutines,
			""
		);
	}
}
