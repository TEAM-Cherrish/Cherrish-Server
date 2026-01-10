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
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.CustomRoutineAddRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.CustomRoutineAddResponseDto;
import com.sopt.cherrish.domain.user.application.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengeCustomRoutineFacade {

	static final int MAX_DAILY_ROUTINE_COUNT = 20;

	private final UserService userService;
	private final ChallengeService challengeService;
	private final ChallengeRoutineService routineService;
	private final ChallengeRoutineRepository routineRepository;
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
		// 1. 사용자 존재 여부 검증
		userService.validateUserExists(userId);

		// 2. 활성 챌린지 조회 (통계와 함께)
		Challenge challenge = challengeService.getActiveChallengeWithStatistics(userId);

		// 3. 오늘 날짜 계산
		LocalDate today = LocalDate.now(clock);

		// 4. 하루 최대 루틴 개수 제한 검증
		long todayRoutineCount = routineRepository.countByChallengeIdAndScheduledDate(
			challenge.getId(), today
		);
		if (todayRoutineCount >= MAX_DAILY_ROUTINE_COUNT) {
			throw new ChallengeException(ChallengeErrorCode.CUSTOM_ROUTINE_LIMIT_EXCEEDED);
		}

		// 5. 커스텀 루틴 Batch Insert
		List<ChallengeRoutine> routines = routineService.createAndSaveCustomRoutine(
			challenge, request.routineName(), today
		);

		// 6. 통계 업데이트 (totalRoutineCount 증가, cherryLevel 재계산)
		ChallengeStatistics statistics = challenge.getStatistics();
		statistics.incrementTotalRoutineCount(routines.size());
		statistics.updateCherryLevel();

		// 7. Response DTO 변환
		return CustomRoutineAddResponseDto.from(
			challenge,
			request.routineName(),
			routines,
			statistics.getTotalRoutineCount()
		);
	}
}
