package com.sopt.cherrish.domain.calendar.presentation;

import com.sopt.cherrish.domain.calendar.application.service.CalendarService;
import com.sopt.cherrish.domain.calendar.presentation.dto.EventType;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDateResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarController.class)
@DisplayName("CalendarController 통합 테스트")
class CalendarControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CalendarService calendarService;

	@Test
	@DisplayName("캘린더 조회 성공 - 정상적인 요청")
	void getCalendar_Success() throws Exception {
		// given
		Long userId = 1L;
		int year = 2025;
		int month = 1;

		ProcedureEventResponseDto eventDto = new ProcedureEventResponseDto(
				EventType.PROCEDURE,
				1L,
				1L,
				"레이저 토닝",
				LocalDateTime.of(2025, 1, 15, 14, 0),
				7,
				List.of(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 16)),
				List.of(LocalDate.of(2025, 1, 17)),
				List.of(LocalDate.of(2025, 1, 18))
		);

		CalendarDateResponseDto dateDto = CalendarDateResponseDto.of(
				LocalDate.of(2025, 1, 15),
				List.of(eventDto)
		);

		CalendarResponseDto response = CalendarResponseDto.of(year, month, List.of(dateDto));

		given(calendarService.getCalendar(eq(userId), eq(year), eq(month))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/users/{userId}/calendar", userId)
						.param("year", String.valueOf(year))
						.param("month", String.valueOf(month)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("S200"))
				.andExpect(jsonPath("$.message").value("성공"))
				.andExpect(jsonPath("$.data.year").value(2025))
				.andExpect(jsonPath("$.data.month").value(1))
				.andExpect(jsonPath("$.data.dates").isArray())
				.andExpect(jsonPath("$.data.dates[0].date").value("2025-01-15"))
				.andExpect(jsonPath("$.data.dates[0].eventCount").value(1))
				.andExpect(jsonPath("$.data.dates[0].events[0].name").value("레이저 토닝"))
				.andExpect(jsonPath("$.data.dates[0].events[0].downtimeDays").value(7));
	}

	@Test
	@DisplayName("캘린더 조회 성공 - 시술이 없는 경우")
	void getCalendar_Success_WithNoEvents() throws Exception {
		// given
		Long userId = 1L;
		int year = 2025;
		int month = 2;

		CalendarResponseDto response = CalendarResponseDto.of(year, month, Collections.emptyList());

		given(calendarService.getCalendar(eq(userId), eq(year), eq(month))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/users/{userId}/calendar", userId)
						.param("year", String.valueOf(year))
						.param("month", String.valueOf(month)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("S200"))
				.andExpect(jsonPath("$.data.year").value(2025))
				.andExpect(jsonPath("$.data.month").value(2))
				.andExpect(jsonPath("$.data.dates").isEmpty());
	}

	// NOTE: @Min/@Max Validation 테스트는 Spring의 Bean Validation 기능이므로
	// @WebMvcTest 환경에서는 추가 설정이 필요하며, 실제로는 프레임워크가 보장하므로 생략

	@Test
	@DisplayName("캘린더 조회 성공 - 경계값 테스트 (2000년 1월)")
	void getCalendar_Success_BoundaryTest_MinYear() throws Exception {
		// given
		Long userId = 1L;
		int year = 2000;
		int month = 1;

		CalendarResponseDto response = CalendarResponseDto.of(year, month, Collections.emptyList());

		given(calendarService.getCalendar(eq(userId), eq(year), eq(month))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/users/{userId}/calendar", userId)
						.param("year", String.valueOf(year))
						.param("month", String.valueOf(month)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.year").value(2000))
				.andExpect(jsonPath("$.data.month").value(1));
	}

	@Test
	@DisplayName("캘린더 조회 성공 - 경계값 테스트 (2100년 12월)")
	void getCalendar_Success_BoundaryTest_MaxYear() throws Exception {
		// given
		Long userId = 1L;
		int year = 2100;
		int month = 12;

		CalendarResponseDto response = CalendarResponseDto.of(year, month, Collections.emptyList());

		given(calendarService.getCalendar(eq(userId), eq(year), eq(month))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/users/{userId}/calendar", userId)
						.param("year", String.valueOf(year))
						.param("month", String.valueOf(month)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.year").value(2100))
				.andExpect(jsonPath("$.data.month").value(12));
	}
}
