package com.sopt.cherrish.domain.maindashboard.application.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// [기존 코드 - 데모 종료 후 복원]
// import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
// import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
// import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeService;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeStatistics;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.MainDashboardResponseDto;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.RecentProcedureResponseDto;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.UpcomingProcedureResponseDto;
import com.sopt.cherrish.domain.user.application.service.UserService;
import com.sopt.cherrish.domain.userprocedure.application.service.UserProcedureService;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

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
	// [기존 코드] private final ChallengeService challengeService;
	private final DemoChallengeService demoChallengeService; // [데모용 코드]
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

		// 3. 챌린지 데이터 (데모 챌린지, 활성 챌린지 없으면 0)
		int cherryLevel = 0;
		int challengeRate = 0;
		String challengeName = null;

		// [기존 코드]
		// Optional<Challenge> challengeOpt = challengeService.findActiveChallengeWithStatistics(userId);
		// if (challengeOpt.isPresent()) {
		// 	Challenge challenge = challengeOpt.get();
		// 	ChallengeStatistics stats = challenge.getStatistics();
		// 	cherryLevel = stats.calculateCherryLevel();
		// 	challengeRate = stats.getProgressPercentage();
		// 	challengeName = challenge.getTitle();
		// } else {
		// 	log.info("사용자 {}의 활성 챌린지 없음 (cherryLevel=0)", userId);
		// }

		// [데모용 코드]
        Optional<DemoChallenge> demoChallengeOpt = demoChallengeService.findActiveChallengeWithStatistics(userId);
		if (demoChallengeOpt.isPresent()) {
			DemoChallenge demoChallenge = demoChallengeOpt.get();
			DemoChallengeStatistics stats = demoChallenge.getStatistics();
			cherryLevel = stats.calculateCherryLevel();
			challengeRate = stats.getProgressPercentage();
			challengeName = demoChallenge.getTitle();
		} else {
			log.info("사용자 {}의 활성 데모 챌린지 없음 (cherryLevel=0)", userId);
		}

		// 4. 최근 시술 (다운타임 진행 중인 모든 시술, Phase/시간순 정렬)
		List<UserProcedure> recentProcedureEntities =
			userProcedureService.findRecentProcedures(userId, today);
		List<RecentProcedureResponseDto> recentProcedures = recentProcedureEntities.stream()
			.map(up -> RecentProcedureResponseDto.from(up, today, up.calculateCurrentPhase(today)))
			.toList();

		// 5. 다가오는 시술 (날짜별 그룹, 가장 가까운 3개 날짜)
		Map<LocalDate, List<UserProcedure>> upcomingProcedureMap =
			userProcedureService.findUpcomingProceduresGroupedByDate(userId, today, MAX_UPCOMING_PROCEDURE_DATES);
		List<UpcomingProcedureResponseDto> upcomingProcedures = upcomingProcedureMap.entrySet().stream()
			.map(entry -> {
				LocalDate date = entry.getKey();
				List<UserProcedure> procedures = entry.getValue();

				// 다운타임 가장 긴 시술 찾기
				UserProcedure longest = procedures.stream()
					.max(Comparator.comparing(UserProcedure::getDowntimeDays))
					.orElseThrow();

				return UpcomingProcedureResponseDto.of(
					date,
					longest.getProcedure().getName(),
					procedures.size(),
					today
				);
			})
			.toList();

		// 6. 응답 생성
		return MainDashboardResponseDto.from(
			today, cherryLevel, challengeRate, challengeName,
			recentProcedures, upcomingProcedures
		);
	}
}
