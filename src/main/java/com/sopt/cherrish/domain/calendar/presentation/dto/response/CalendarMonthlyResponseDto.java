package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "월별 캘린더 응답")
public record CalendarMonthlyResponseDto(
	@Schema(description = "일자별 시술 개수 (key: 일자, value: 시술 개수)", example = "{\"1\": 2, \"5\": 1, \"10\": 3}")
	Map<Integer, Long> dailyProcedureCounts
) {
	public static CalendarMonthlyResponseDto from(Map<Integer, Long> dailyProcedureCounts) {
		return new CalendarMonthlyResponseDto(dailyProcedureCounts);
	}
}
