package com.sopt.cherrish.domain.calendar.presentation;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.sopt.cherrish.domain.calendar.application.service.CalendarService;
import com.sopt.cherrish.domain.calendar.presentation.dto.request.CalendarDailyRequestDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.request.CalendarMonthlyRequestDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDailyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarMonthlyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventDowntimeResponseDto;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.userprocedure.exception.UserProcedureErrorCode;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
		@Valid @ModelAttribute CalendarMonthlyRequestDto request
	) {
		CalendarMonthlyResponseDto response = calendarService.getMonthlyCalendar(
			userId,
			request.year(),
			request.month()
		);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	@Operation(
		summary = "일자별 시술 상세 조회",
		description = "특정 날짜의 시술 목록을 조회합니다."
	)
	@ApiExceptions({UserErrorCode.class, ErrorCode.class})
	@GetMapping("/daily")
	public CommonApiResponse<CalendarDailyResponseDto> getDailyCalendar(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Valid @ModelAttribute CalendarDailyRequestDto request
	) {
		CalendarDailyResponseDto response = calendarService.getDailyCalendar(userId, request.date());
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	@Operation(
		summary = "시술 다운타임 상세 조회",
		description = "특정 시술 일정의 다운타임 기간(민감기/주의기/회복기)을 조회합니다."
	)
	@ApiExceptions({UserErrorCode.class, UserProcedureErrorCode.class, ErrorCode.class})
	@GetMapping("/events/{id}/downtime")
	public CommonApiResponse<ProcedureEventDowntimeResponseDto> getEventDowntime(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Parameter(description = "사용자 시술 일정 ID", required = true, example = "123")
		@PathVariable("id") @Positive Long userProcedureId
	) {
		ProcedureEventDowntimeResponseDto response = calendarService.getEventDowntime(userId, userProcedureId);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
