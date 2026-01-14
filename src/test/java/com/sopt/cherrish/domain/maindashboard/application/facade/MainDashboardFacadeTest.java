package com.sopt.cherrish.domain.maindashboard.application.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.MainDashboardResponseDto;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.RecentProcedureResponseDto;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.UpcomingProcedureResponseDto;
import com.sopt.cherrish.domain.procedure.fixture.ProcedureFixture;
import com.sopt.cherrish.domain.user.application.service.UserService;
import com.sopt.cherrish.domain.user.fixture.UserFixture;
import com.sopt.cherrish.domain.userprocedure.application.service.UserProcedureService;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("MainDashboardFacade 단위 테스트")
class MainDashboardFacadeTest {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private MainDashboardFacade mainDashboardFacade;

	@Mock
	private UserService userService;

	@Mock
	private ChallengeService challengeService;

	@Mock
	private UserProcedureService userProcedureService;

	private LocalDate today;

	@BeforeEach
	void setUp() {
		today = LocalDate.of(2026, 1, 15);
		Clock fixedClock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), KST);
		mainDashboardFacade = new MainDashboardFacade(
			userService, challengeService, userProcedureService, fixedClock
		);
	}

	@Test
	@DisplayName("활성 챌린지/최근 시술이 없으면 기본값과 빈 리스트 반환")
	void getMainDashboardDefaults() {
		// given
		Long userId = 1L;
		given(challengeService.findActiveChallengeWithStatistics(userId))
			.willReturn(Optional.empty());
		given(userProcedureService.findRecentProcedures(userId, today))
			.willReturn(List.of());
		given(userProcedureService.findUpcomingProceduresGroupedByDate(userId, today, 3))
			.willReturn(Map.of());

		// when
		MainDashboardResponseDto result = mainDashboardFacade.getMainDashboard(userId);

		// then
		assertThat(result.getCherryLevel()).isEqualTo(0);
		assertThat(result.getChallengeRate()).isEqualTo(0.0);
		assertThat(result.getChallengeName()).isNull();
		assertThat(result.getDayOfWeek()).isEqualTo(today.getDayOfWeek().name());
		assertThat(result.getRecentProcedures()).isEmpty();
		assertThat(result.getUpcomingProcedures()).isEmpty();
	}

	@Test
	@DisplayName("활성 챌린지가 없으면 체리 레벨 0으로 반환")
	void getMainDashboardWithoutActiveChallenge() {
		// given
		Long userId = 2L;
		given(challengeService.findActiveChallengeWithStatistics(userId))
			.willReturn(Optional.empty());

		var user = UserFixture.createUser();
		UserProcedure recent = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("레이저", "레이저", 0, 5),
			LocalDateTime.of(2026, 1, 14, 10, 0),
			6
		);
		given(userProcedureService.findRecentProcedures(userId, today))
			.willReturn(List.of(recent));
		given(userProcedureService.findUpcomingProceduresGroupedByDate(userId, today, 3))
			.willReturn(Map.of());

		// when
		MainDashboardResponseDto result = mainDashboardFacade.getMainDashboard(userId);

		// then
		assertThat(result.getCherryLevel()).isEqualTo(0);
		assertThat(result.getChallengeRate()).isEqualTo(0.0);
		assertThat(result.getChallengeName()).isNull();
		assertThat(result.getRecentProcedures()).isNotNull();
	}

	@Test
	@DisplayName("다운타임 중인 시술이 없으면 recentProcedures는 빈 리스트")
	void getMainDashboardWithoutRecentProcedures() {
		// given
		Long userId = 3L;
		Challenge challenge = org.mockito.Mockito.mock(Challenge.class);
		ChallengeStatistics stats = org.mockito.Mockito.mock(ChallengeStatistics.class);
		given(challengeService.findActiveChallengeWithStatistics(userId))
			.willReturn(Optional.of(challenge));
		given(challenge.getStatistics()).willReturn(stats);
		given(challenge.getTitle()).willReturn("7일 보습 챌린지");
		given(stats.calculateCherryLevel()).willReturn(2);
		given(stats.getProgressPercentage()).willReturn(40.0);

		given(userProcedureService.findRecentProcedures(userId, today))
			.willReturn(List.of());

		var user = UserFixture.createUser();
		Map<LocalDate, List<UserProcedure>> upcoming = new LinkedHashMap<>();
		upcoming.put(
			LocalDate.of(2026, 1, 16),
			List.of(
				UserProcedureFixture.createUserProcedure(
					user,
					ProcedureFixture.createProcedure("보톡스", "주사", 0, 5),
					LocalDateTime.of(2026, 1, 16, 9, 0),
					2
				)
			)
		);
		given(userProcedureService.findUpcomingProceduresGroupedByDate(userId, today, 3))
			.willReturn(upcoming);

		// when
		MainDashboardResponseDto result = mainDashboardFacade.getMainDashboard(userId);

		// then
		assertThat(result.getChallengeName()).isEqualTo("7일 보습 챌린지");
		assertThat(result.getRecentProcedures()).isEmpty();
		assertThat(result.getUpcomingProcedures()).hasSize(1);
	}

	@Test
	@DisplayName("다가오는 시술이 없으면 upcomingProcedures는 빈 리스트")
	void getMainDashboardWithoutUpcomingProcedures() {
		// given
		Long userId = 4L;
		Challenge challenge = org.mockito.Mockito.mock(Challenge.class);
		ChallengeStatistics stats = org.mockito.Mockito.mock(ChallengeStatistics.class);
		given(challengeService.findActiveChallengeWithStatistics(userId))
			.willReturn(Optional.of(challenge));
		given(challenge.getStatistics()).willReturn(stats);
		given(challenge.getTitle()).willReturn("7일 보습 챌린지");
		given(stats.calculateCherryLevel()).willReturn(1);
		given(stats.getProgressPercentage()).willReturn(10.0);

		var user = UserFixture.createUser();
		UserProcedure recent = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("레이저", "레이저", 0, 5),
			LocalDateTime.of(2026, 1, 14, 10, 0),
			6
		);
		given(userProcedureService.findRecentProcedures(userId, today))
			.willReturn(List.of(recent));
		given(userProcedureService.findUpcomingProceduresGroupedByDate(userId, today, 3))
			.willReturn(Map.of());

		// when
		MainDashboardResponseDto result = mainDashboardFacade.getMainDashboard(userId);

		// then
		assertThat(result.getChallengeName()).isEqualTo("7일 보습 챌린지");
		assertThat(result.getUpcomingProcedures()).isEmpty();
		assertThat(result.getRecentProcedures()).isNotNull();
	}

	@Test
	@DisplayName("여러 날짜에 걸친 다운타임 진행 중인 시술 응답")
	void getMainDashboardWithMultipleDateRecentProcedures() {
		// given
		Long userId = 5L;
		Challenge challenge = org.mockito.Mockito.mock(Challenge.class);
		ChallengeStatistics stats = org.mockito.Mockito.mock(ChallengeStatistics.class);
		given(challengeService.findActiveChallengeWithStatistics(userId))
			.willReturn(Optional.of(challenge));
		given(challenge.getStatistics()).willReturn(stats);
		given(challenge.getTitle()).willReturn("7일 보습 챌린지");
		given(stats.calculateCherryLevel()).willReturn(3);
		given(stats.getProgressPercentage()).willReturn(65.0);

		var user = UserFixture.createUser();
		// 1/10 시술 (다운타임 7일) - 1/15 기준 RECOVERY
		UserProcedure jan10 = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("필러", "주사", 0, 5),
			LocalDateTime.of(2026, 1, 10, 9, 0),
			7
		);
		// 1/12 시술 (다운타임 5일) - 1/15 기준 CAUTION
		UserProcedure jan12 = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("보톡스", "주사", 0, 5),
			LocalDateTime.of(2026, 1, 12, 10, 0),
			5
		);
		// 1/14 시술 (다운타임 4일) - 1/15 기준 SENSITIVE
		UserProcedure jan14 = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("레이저", "레이저", 0, 5),
			LocalDateTime.of(2026, 1, 14, 14, 0),
			4
		);

		given(userProcedureService.findRecentProcedures(userId, today))
			.willReturn(List.of(jan14, jan12, jan10));
		given(userProcedureService.findUpcomingProceduresGroupedByDate(userId, today, 3))
			.willReturn(Map.of());

		// when
		MainDashboardResponseDto result = mainDashboardFacade.getMainDashboard(userId);

		// then
		assertThat(result.getCherryLevel()).isEqualTo(3);
		assertThat(result.getChallengeRate()).isEqualTo(65.0);
		assertThat(result.getChallengeName()).isEqualTo("7일 보습 챌린지");

		List<RecentProcedureResponseDto> recent = result.getRecentProcedures();
		assertThat(recent).hasSize(3);

		// SENSITIVE - 레이저 (1/14 시술, 2일차)
		assertThat(recent.get(0).getName()).isEqualTo("레이저");
		assertThat(recent.get(0).getDaysSince()).isEqualTo(2);  // 1/14 → 1/15 = 1일 경과 + 1 = 2일차

		// CAUTION - 보톡스 (1/12 시술, 4일차)
		assertThat(recent.get(1).getName()).isEqualTo("보톡스");
		assertThat(recent.get(1).getDaysSince()).isEqualTo(4);  // 1/12 → 1/15 = 3일 경과 + 1 = 4일차

		// RECOVERY - 필러 (1/10 시술, 6일차)
		assertThat(recent.get(2).getName()).isEqualTo("필러");
		assertThat(recent.get(2).getDaysSince()).isEqualTo(6);  // 1/10 → 1/15 = 5일 경과 + 1 = 6일차
	}

	@Test
	@DisplayName("챌린지/시술 데이터가 있으면 응답 매핑")
	void getMainDashboardWithChallengeAndProcedures() {
		// given
		Long userId = 1L;
		Challenge challenge = org.mockito.Mockito.mock(Challenge.class);
		ChallengeStatistics stats = org.mockito.Mockito.mock(ChallengeStatistics.class);
		given(challengeService.findActiveChallengeWithStatistics(userId))
			.willReturn(Optional.of(challenge));
		given(challenge.getStatistics()).willReturn(stats);
		given(challenge.getTitle()).willReturn("7일 보습 챌린지");
		given(stats.calculateCherryLevel()).willReturn(4);
		given(stats.getProgressPercentage()).willReturn(80.0);

		var user = UserFixture.createUser();
		UserProcedure recent1 = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("레이저", "레이저", 0, 5),
			LocalDateTime.of(2026, 1, 14, 10, 0),
			6
		);
		UserProcedure recent2 = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("필러", "주사", 0, 5),
			LocalDateTime.of(2026, 1, 13, 10, 0),
			3
		);
		given(userProcedureService.findRecentProcedures(userId, today))
			.willReturn(List.of(recent1, recent2));

		Map<LocalDate, List<UserProcedure>> upcoming = new LinkedHashMap<>();
		upcoming.put(
			LocalDate.of(2026, 1, 16),
			List.of(
				UserProcedureFixture.createUserProcedure(
					user,
					ProcedureFixture.createProcedure("보톡스", "주사", 0, 5),
					LocalDateTime.of(2026, 1, 16, 9, 0),
					2
				),
				UserProcedureFixture.createUserProcedure(
					user,
					ProcedureFixture.createProcedure("울쎄라", "레이저", 0, 5),
					LocalDateTime.of(2026, 1, 16, 12, 0),
					7
				)
			)
		);
		upcoming.put(
			LocalDate.of(2026, 1, 17),
			List.of(
				UserProcedureFixture.createUserProcedure(
					user,
					ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 5),
					LocalDateTime.of(2026, 1, 17, 9, 0),
					1
				)
			)
		);
		given(userProcedureService.findUpcomingProceduresGroupedByDate(userId, today, 3))
			.willReturn(upcoming);

		// when
		MainDashboardResponseDto result = mainDashboardFacade.getMainDashboard(userId);

		// then
		assertThat(result.getCherryLevel()).isEqualTo(4);
		assertThat(result.getChallengeRate()).isEqualTo(80.0);
		assertThat(result.getChallengeName()).isEqualTo("7일 보습 챌린지");

		List<RecentProcedureResponseDto> recent = result.getRecentProcedures();
		assertThat(recent).hasSize(2);
		assertThat(recent.get(0).getName()).isEqualTo("레이저");
		assertThat(recent.get(0).getDaysSince()).isPositive();
		assertThat(recent.get(1).getName()).isEqualTo("필러");
		assertThat(recent.get(1).getDaysSince()).isPositive();

		List<UpcomingProcedureResponseDto> upcomingDtos = result.getUpcomingProcedures();
		assertThat(upcomingDtos).hasSize(2);
		assertThat(upcomingDtos.get(0).getDate()).isEqualTo(LocalDate.of(2026, 1, 16));
		assertThat(upcomingDtos.get(0).getName()).isEqualTo("울쎄라");
		assertThat(upcomingDtos.get(0).getCount()).isEqualTo(2);
		assertThat(upcomingDtos.get(0).getDDay()).isEqualTo(1);
		assertThat(upcomingDtos.get(1).getDate()).isEqualTo(LocalDate.of(2026, 1, 17));
		assertThat(upcomingDtos.get(1).getName()).isEqualTo("레이저 토닝");
		assertThat(upcomingDtos.get(1).getCount()).isEqualTo(1);
		assertThat(upcomingDtos.get(1).getDDay()).isEqualTo(2);
	}
}
