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
		// 1. 활성 챌린지 조회 (통계와 함께 Fetch Join)
		DemoChallenge challenge = challengeService.getActiveChallengeWithStatistics(userId);

		// 2. 가상 날짜 기준으로 오늘의 루틴 조회
		LocalDate currentDate = challenge.getCurrentVirtualDate();
		List<DemoChallengeRoutine> todayRoutines = routineService.getRoutinesByDate(challenge.getId(), currentDate);

		// 3. 통계는 Challenge에서 가져옴 (이미 Fetch Join으로 로드됨)
		DemoChallengeStatistics statistics = challenge.getStatistics();

		// 4. 현재 일차 계산
		int currentDay = challenge.getCurrentDay();

		// 5. 응답 DTO 생성
		return ChallengeDetailResponseDto.from(
			challenge,
			currentDay,
			statistics,
			todayRoutines,
			""  // 응원 메시지 없음
		);
	}
}
