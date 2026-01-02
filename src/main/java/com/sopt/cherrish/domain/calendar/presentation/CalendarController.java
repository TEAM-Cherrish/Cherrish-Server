package com.sopt.cherrish.domain.calendar.presentation;

import com.sopt.cherrish.domain.calendar.application.service.CalendarService;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarResponseDto;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Calendar", description = "캘린더 관련 API")
@RestController
@RequestMapping("/api/users/{userId}/calendar")
@RequiredArgsConstructor
public class CalendarController {

	private final CalendarService calendarService;

	@Operation(
			summary = "캘린더 조회",
			description = "지정한 연도와 월의 캘린더 정보를 조회합니다. 해당 월의 시술 일정과 다운타임 정보를 포함합니다."
	)
	@GetMapping
	public CommonApiResponse<CalendarResponseDto> getCalendar(
			@Parameter(description = "사용자 ID", example = "1", required = true)
			@PathVariable
			Long userId,

			@Parameter(description = "조회 연도", example = "2026", required = true)
			@RequestParam
			int year,

			@Parameter(description = "조회 월 (1-12)", example = "1", required = true)
			@RequestParam
			int month
	) {
		CalendarResponseDto calendar = calendarService.getCalendar(userId, year, month);
		return CommonApiResponse.success(SuccessCode.SUCCESS, calendar);
	}
}
