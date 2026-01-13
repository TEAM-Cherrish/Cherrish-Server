package com.sopt.cherrish.domain.calendar.presentation;

import static com.sopt.cherrish.domain.calendar.fixture.CalendarTestFixture.createDailyResponseWithSingleEvent;
import static com.sopt.cherrish.domain.calendar.fixture.CalendarTestFixture.createDowntimeResponse;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sopt.cherrish.domain.calendar.application.service.CalendarService;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDailyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarMonthlyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventDowntimeResponseDto;

@WebMvcTest(CalendarController.class)
@DisplayName("CalendarController 통합 테스트")
class CalendarControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CalendarService calendarService;

	@Test
	@DisplayName("월별 캘린더 조회 성공")
	void getMonthlyCalendarSuccess() throws Exception {
		// given
		Long userId = 1L;
		int year = 2025;
		int month = 1;

		Map<Integer, Long> mockCounts = Map.of(
			1, 2L,
			5, 1L,
			10, 3L
		);

		CalendarMonthlyResponseDto response = CalendarMonthlyResponseDto.from(mockCounts);
		given(calendarService.getMonthlyCalendar(userId, year, month)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/calendar/monthly")
				.header("X-User-Id", userId)
				.param("year", String.valueOf(year))
				.param("month", String.valueOf(month)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.dailyProcedureCounts['1']").value(2))
			.andExpect(jsonPath("$.data.dailyProcedureCounts['5']").value(1))
			.andExpect(jsonPath("$.data.dailyProcedureCounts['10']").value(3));
	}

	@Test
	@DisplayName("월별 캘린더 조회 실패 - year 파라미터 누락")
	void getMonthlyCalendarMissingYear() throws Exception {
		mockMvc.perform(get("/api/calendar/monthly")
				.header("X-User-Id", 1L)
				.param("month", "1"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("월별 캘린더 조회 실패 - month 파라미터 누락")
	void getMonthlyCalendarMissingMonth() throws Exception {
		mockMvc.perform(get("/api/calendar/monthly")
				.header("X-User-Id", 1L)
				.param("year", "2025"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("월별 캘린더 조회 실패 - X-User-Id 헤더 누락")
	void getMonthlyCalendarMissingUserId() throws Exception {
		mockMvc.perform(get("/api/calendar/monthly")
				.param("year", "2025")
				.param("month", "1"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("일자별 시술 상세 조회 성공")
	void getDailyCalendarSuccess() throws Exception {
		// given
		Long userId = 1L;
		LocalDate date = LocalDate.of(2025, 1, 15);

		CalendarDailyResponseDto response = createDailyResponseWithSingleEvent(
			123L,
			5L,
			"레이저 토닝",
			LocalDateTime.of(2025, 1, 15, 14, 0),
			9
		);

		given(calendarService.getDailyCalendar(userId, date)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/calendar/daily")
				.header("X-User-Id", userId)
				.param("date", "2025-01-15"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.eventCount").value(1))
			.andExpect(jsonPath("$.data.events[0].type").value("PROCEDURE"))
			.andExpect(jsonPath("$.data.events[0].userProcedureId").value(123))
			.andExpect(jsonPath("$.data.events[0].procedureId").value(5))
			.andExpect(jsonPath("$.data.events[0].name").value("레이저 토닝"))
			.andExpect(jsonPath("$.data.events[0].scheduledAt").value("2025-01-15T14:00:00"))
			.andExpect(jsonPath("$.data.events[0].downtimeDays").value(9));
	}

	@Test
	@DisplayName("일자별 시술 상세 조회 실패 - date 파라미터 누락")
	void getDailyCalendarMissingDate() throws Exception {
		mockMvc.perform(get("/api/calendar/daily")
				.header("X-User-Id", 1L))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("일자별 시술 상세 조회 실패 - X-User-Id 헤더 누락")
	void getDailyCalendarMissingUserId() throws Exception {
		mockMvc.perform(get("/api/calendar/daily")
				.param("date", "2025-01-15"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("일자별 시술 상세 조회 실패 - date 형식 오류")
	void getDailyCalendarInvalidDateFormat() throws Exception {
		mockMvc.perform(get("/api/calendar/daily")
				.header("X-User-Id", 1L)
				.param("date", "2025/01/15"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("시술 다운타임 상세 조회 성공")
	void getEventDowntimeSuccess() throws Exception {
		// given
		Long userId = 1L;
		Long userProcedureId = 123L;

		ProcedureEventDowntimeResponseDto response = createDowntimeResponse(
			userProcedureId,
			LocalDateTime.of(2025, 1, 15, 14, 0),
			9,
			List.of(
				LocalDate.of(2025, 1, 15),
				LocalDate.of(2025, 1, 16),
				LocalDate.of(2025, 1, 17)
			),
			List.of(
				LocalDate.of(2025, 1, 18),
				LocalDate.of(2025, 1, 19),
				LocalDate.of(2025, 1, 20)
			),
			List.of(
				LocalDate.of(2025, 1, 21),
				LocalDate.of(2025, 1, 22),
				LocalDate.of(2025, 1, 23)
			)
		);

		given(calendarService.getEventDowntime(userId, userProcedureId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/calendar/events/{id}/downtime", userProcedureId)
				.header("X-User-Id", userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.userProcedureId").value(123))
			.andExpect(jsonPath("$.data.downtimeDays").value(9))
			.andExpect(jsonPath("$.data.sensitiveDays").isArray())
			.andExpect(jsonPath("$.data.sensitiveDays.length()").value(3))
			.andExpect(jsonPath("$.data.cautionDays").isArray())
			.andExpect(jsonPath("$.data.cautionDays.length()").value(3))
			.andExpect(jsonPath("$.data.recoveryDays").isArray())
		    .andExpect(jsonPath("$.data.recoveryDays.length()").value(3));
	}

	@ParameterizedTest
	@ValueSource(longs = {0L, -1L})
	@DisplayName("시술 다운타임 상세 조회 실패 - 잘못된 사용자 시술 일정 ID")
	void getEventDowntimeInvalidId(long userProcedureId) throws Exception {
		mockMvc.perform(get("/api/calendar/events/{id}/downtime", userProcedureId)
				.header("X-User-Id", 1L))
			.andExpect(status().isBadRequest());
	}
}
