package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CalendarResponseDto(
		@Schema(description = "조회 연도", example = "2025")
        @NotNull(message = "연도는 필수 입력값입니다.")
        @Min(value = 2000, message = "연도는 2000년 이상이어야 합니다.")
        @Max(value = 2100, message = "연도는 2100년 이하여야 합니다.")
		int year,

		@Schema(description = "조회 월 (1-12)", example = "1")
        @NotNull(message = "월은 필수 입력값입니다.")
        @Min(value = 1, message = "월은 1 이상이어야 합니다.")
        @Max(value = 12, message = "월은 12 이하여야 합니다.")
		int month,

		@Schema(description = "날짜별 이벤트 목록")
        @NotNull(message = "날짜 목록은 null일 수 없습니다.")
		List<CalendarDateDto> dates
) {
	public static CalendarResponseDto of(int year, int month, List<CalendarDateDto> dates) {
		return new CalendarResponseDto(year, month, dates);
	}
}
