package com.sopt.cherrish.domain.calendar.presentation;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.sopt.cherrish.domain.calendar.application.service.CalendarService;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDailyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarMonthlyResponseDto;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Validated
@Tag(name = "Calendar", description = "캘린더 조회 API")
public class CalendarController {

	private final CalendarService calendarService;

	@Operation(
		summary = "월별 캘린더 조회",
		description = "특정 연월의 일자별 시술 개수를 조회합니다."
	)
	@ApiExceptions({UserErrorCode.class, ErrorCode.class})
	@GetMapping("/monthly")
	public CommonApiResponse<CalendarMonthlyResponseDto> getMonthlyCalendar(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Parameter(description = "연도", required = true, example = "2026")
		@RequestParam @Min(2000) @Max(2100) int year,
		@Parameter(description = "월", required = true, example = "1")
		@RequestParam @Min(1) @Max(12) int month
	) {
		CalendarMonthlyResponseDto response = calendarService.getMonthlyCalendar(userId, year, month);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	@Operation(
		summary = "일자별 시술 상세 조회",
		description = "특정 날짜의 시술 목록과 다운타임 기간(민감기/주의기/회복기)을 조회합니다."
	)
	@ApiExceptions({UserErrorCode.class, ErrorCode.class})
	@GetMapping("/daily")
	public CommonApiResponse<CalendarDailyResponseDto> getDailyCalendar(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Parameter(description = "날짜", required = true, example = "2026-01-15")
		@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
	) {
		CalendarDailyResponseDto response = calendarService.getDailyCalendar(userId, date);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
