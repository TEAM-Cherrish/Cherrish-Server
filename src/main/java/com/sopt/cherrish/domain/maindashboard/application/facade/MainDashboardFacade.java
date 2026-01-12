package com.sopt.cherrish.domain.maindashboard.application.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.MainDashboardResponseDto;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.RecentProcedureResponseDto;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.UpcomingProcedureResponseDto;
import com.sopt.cherrish.domain.user.application.service.UserService;
import com.sopt.cherrish.domain.userprocedure.application.service.UserProcedureService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메인 대시보드 Facade
 * 여러 도메인(User, Challenge, UserProcedure)의 서비스를 조합하여 대시보드 데이터를 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MainDashboardFacade {

    private static final int MAX_UPCOMING_PROCEDURE_DATES = 3;

	private final UserService userService;
	private final ChallengeService challengeService;
	private final UserProcedureService userProcedureService;
	private final Clock clock;

	/**
	 * 메인 대시보드 조회
	 * @param userId 사용자 ID
	 * @return 메인 대시보드 응답
	 */
	public MainDashboardResponseDto getMainDashboard(Long userId) {
		// 1. 사용자 존재 여부 확인
		userService.validateUserExists(userId);

		// 2. 오늘 날짜 가져오기
		LocalDate today = LocalDate.now(clock);

		// 3. 챌린지 데이터 (활성 챌린지 없으면 0)
		Integer cherryLevel = 0;
		Double challengeRate = 0.0;

		var challengeOpt = challengeService.findActiveChallengeWithStatistics(userId);
		if (challengeOpt.isPresent()) {
			Challenge challenge = challengeOpt.get();
			ChallengeStatistics stats = challenge.getStatistics();
			cherryLevel = stats.calculateCherryLevel();
			challengeRate = stats.getProgressPercentage();
		} else {
			log.info("사용자 {}의 활성 챌린지 없음 (cherryLevel=0)", userId);
		}

		// 4. 최근 시술 (가장 최근 날짜의 모든 시술, COMPLETED 제외)
		List<RecentProcedureResponseDto> recentProcedures =
			userProcedureService.findRecentProcedures(userId, today);

		// 5. 다가오는 시술 (날짜별 그룹, 가장 가까운 3개 날짜)
		List<UpcomingProcedureResponseDto> upcomingProcedures =
			userProcedureService.findUpcomingProceduresGroupedByDate(userId, today, MAX_UPCOMING_PROCEDURE_DATES);

		// 6. 응답 생성
		return MainDashboardResponseDto.from(
			today, cherryLevel, challengeRate,
			recentProcedures, upcomingProcedures
		);
	}
}
