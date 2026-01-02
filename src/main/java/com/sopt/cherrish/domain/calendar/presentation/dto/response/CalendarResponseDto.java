package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CalendarResponseDto(
		@Schema(description = "조회 연도", example = "2025")
		int year,

		@Schema(description = "조회 월 (1-12)", example = "1")
		int month,

		@Schema(description = "날짜별 이벤트 목록")
		List<CalendarDateResponseDto> dates
) {
	public static CalendarResponseDto of(int year, int month, List<CalendarDateResponseDto> dates) {
		return new CalendarResponseDto(year, month, dates);
	}
}
