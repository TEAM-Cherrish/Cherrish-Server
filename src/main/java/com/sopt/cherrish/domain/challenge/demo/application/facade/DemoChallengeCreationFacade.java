package com.sopt.cherrish.domain.challenge.demo.application.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeService;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeStatisticsService;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeRoutine;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DemoChallengeCreationFacade {

	private final DemoChallengeService challengeService;
	private final DemoChallengeRoutineService routineService;
	private final DemoChallengeStatisticsService statisticsService;
	private final UserRepository userRepository;
	private final Clock clock;

	/**
	 * 데모 챌린지 생성 플로우 오케스트레이션
	 */
	@Transactional
	public ChallengeCreateResponseDto createChallenge(
		Long userId, ChallengeCreateRequestDto request) {

		// 1. User 존재 확인 (Fail-Fast)
		if (!userRepository.existsById(userId)) {
			throw new UserException(UserErrorCode.USER_NOT_FOUND);
		}

		// 2. 활성 챌린지 중복 확인
		challengeService.validateNoDuplicateActiveChallenge(userId);

		// 3. HomecareRoutine 변환 및 유효성 검증
		HomecareRoutine routine = HomecareRoutine.fromId(request.homecareRoutineId());

		// 4. 챌린지 생성
		LocalDate startDate = LocalDate.now(clock);

		DemoChallenge challenge = challengeService.createChallenge(userId, routine, startDate);

		// 5. 챌린지 루틴 Batch Insert
		List<DemoChallengeRoutine> routines = routineService.createAndSaveRoutines(
			challenge, request.routineNames());

		// 6. 통계 초기화
		statisticsService.initializeStatistics(challenge, routines);

		// 7. Response DTO 변환
		return ChallengeCreateResponseDto.from(challenge, routines, routines.size());
	}
}
