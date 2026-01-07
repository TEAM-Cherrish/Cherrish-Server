package com.sopt.cherrish.domain.challenge.core.application.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.application.service.CheeringMessageGenerator;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
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
	private final CheeringMessageGenerator cheeringMessageGenerator;
	private final Clock clock;

	/**
	 * 활성 챌린지 상세 조회
	 *
	 * 쿼리 최적화:
	 * - Challenge와 Statistics를 Fetch Join으로 한 번에 조회 (쿼리 1)
	 * - 오늘의 루틴을 별도 조회 (쿼리 2)
	 *
	 * @param userId 사용자 ID
	 * @return 챌린지 상세 응답
	 */
	@Transactional(readOnly = true)
	public ChallengeDetailResponseDto getActiveChallengeDetail(Long userId) {
		// 0. 오늘 날짜 계산 (한 번만)
		LocalDate today = LocalDate.now(clock);

		// 1. 활성 챌린지 조회 (통계와 함께 Fetch Join으로 한 번에 조회)
		Challenge challenge = challengeService.getActiveChallengeWithStatistics(userId);

		// 2. 오늘의 루틴 조회 (날짜를 직접 전달)
		List<ChallengeRoutine> todayRoutines = routineService.getRoutinesByDate(challenge.getId(), today);

		// 3. 통계는 Challenge에서 가져옴 (이미 Fetch Join으로 로드됨)
		ChallengeStatistics statistics = challenge.getStatistics();

		// 4. 현재 일차 계산
		int currentDay = challenge.getCurrentDay(today);

		// 5. 응원 메시지 생성
		String cheeringMessage = cheeringMessageGenerator.generate(currentDay, challenge.getTotalDays());

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
