package com.sopt.cherrish.domain.maindashboard.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sopt.cherrish.domain.maindashboard.application.facade.MainDashboardFacade;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.MainDashboardResponseDto;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.RecentProcedureResponseDto;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.UpcomingProcedureResponseDto;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.userprocedure.domain.model.ProcedurePhase;

@WebMvcTest(MainDashboardController.class)
@DisplayName("MainDashboardController 테스트")
class MainDashboardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MainDashboardFacade mainDashboardFacade;

	@Test
	@DisplayName("성공 - 메인 대시보드 조회")
	void getMainDashboardSuccess() throws Exception {
		// given
		Long userId = 1L;
		LocalDate today = LocalDate.of(2026, 1, 15);
		RecentProcedureResponseDto recent = RecentProcedureResponseDto.builder()
			.name("레이저 토닝")
			.daysSince(2)
			.currentPhase(ProcedurePhase.CAUTION)
			.build();
		UpcomingProcedureResponseDto upcoming = UpcomingProcedureResponseDto.of(
			LocalDate.of(2026, 1, 16),
			"보톡스",
			2,
			today
		);
		MainDashboardResponseDto response = MainDashboardResponseDto.from(
			today,
			3,
			55.5,
			"7일 보습 챌린지",
			List.of(recent),
			List.of(upcoming)
		);

		given(mainDashboardFacade.getMainDashboard(userId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/main-dashboard")
				.header("X-User-Id", userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.date").value("2026-01-15"))
			.andExpect(jsonPath("$.data.cherryLevel").value(3))
			.andExpect(jsonPath("$.data.challengeRate").value(55.5))
			.andExpect(jsonPath("$.data.challengeName").value("7일 보습 챌린지"))
			.andExpect(jsonPath("$.data.recentProcedures[0].name").value("레이저 토닝"))
			.andExpect(jsonPath("$.data.upcomingProcedures[0].name").value("보톡스"));
	}

	@Test
	@DisplayName("실패 - 존재하지 않는 사용자")
	void getMainDashboardUserNotFound() throws Exception {
		// given
		Long invalidUserId = 999L;
		given(mainDashboardFacade.getMainDashboard(invalidUserId))
			.willThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

		// when & then
		mockMvc.perform(get("/api/main-dashboard")
				.header("X-User-Id", invalidUserId))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("성공 - 챌린지 없을 때 cherryLevel 0 반환")
	void getMainDashboardWithoutChallenge() throws Exception {
		// given
		Long userId = 2L;
		LocalDate today = LocalDate.of(2026, 1, 15);
		MainDashboardResponseDto response = MainDashboardResponseDto.from(
			today,
			0,  // cherryLevel = 0
			0.0,
			null,
			List.of(),
			List.of()
		);
		given(mainDashboardFacade.getMainDashboard(userId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/main-dashboard")
				.header("X-User-Id", userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.cherryLevel").value(0))
			.andExpect(jsonPath("$.data.challengeRate").value(0.0))
			.andExpect(jsonPath("$.data.recentProcedures").isArray())
			.andExpect(jsonPath("$.data.recentProcedures").isEmpty())
			.andExpect(jsonPath("$.data.upcomingProcedures").isArray())
			.andExpect(jsonPath("$.data.upcomingProcedures").isEmpty());
	}

	@Test
	@DisplayName("성공 - recentProcedures 빈 배열 반환")
	void getMainDashboardWithEmptyRecentProcedures() throws Exception {
		// given
		Long userId = 3L;
		LocalDate today = LocalDate.of(2026, 1, 15);
		UpcomingProcedureResponseDto upcoming = UpcomingProcedureResponseDto.of(
			LocalDate.of(2026, 1, 20),
			"보톡스",
			1,
			today
		);
		MainDashboardResponseDto response = MainDashboardResponseDto.from(
			today,
			2,
			45.0,
			"7일 보습 챌린지",
			List.of(),  // 빈 리스트
			List.of(upcoming)
		);
		given(mainDashboardFacade.getMainDashboard(userId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/main-dashboard")
				.header("X-User-Id", userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.recentProcedures").isArray())
			.andExpect(jsonPath("$.data.recentProcedures").isEmpty())
			.andExpect(jsonPath("$.data.upcomingProcedures").isArray())
			.andExpect(jsonPath("$.data.upcomingProcedures[0].name").value("보톡스"));
	}

	@Test
	@DisplayName("성공 - upcomingProcedures 빈 배열 반환")
	void getMainDashboardWithEmptyUpcomingProcedures() throws Exception {
		// given
		Long userId = 4L;
		LocalDate today = LocalDate.of(2026, 1, 15);
		RecentProcedureResponseDto recent = RecentProcedureResponseDto.builder()
			.name("필러")
			.daysSince(3)
			.currentPhase(ProcedurePhase.RECOVERY)
			.build();
		MainDashboardResponseDto response = MainDashboardResponseDto.from(
			today,
			3,
			60.0,
			"7일 보습 챌린지",
			List.of(recent),
			List.of()  // 빈 리스트
		);
		given(mainDashboardFacade.getMainDashboard(userId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/main-dashboard")
				.header("X-User-Id", userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.recentProcedures").isArray())
			.andExpect(jsonPath("$.data.recentProcedures[0].name").value("필러"))
			.andExpect(jsonPath("$.data.upcomingProcedures").isArray())
			.andExpect(jsonPath("$.data.upcomingProcedures").isEmpty());
	}
}
