package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record CalendarDateResponseDto(
		@Schema(description = "날짜", example = "2025-01-15")
		LocalDate date,

		@Schema(description = "해당 날짜의 시술 수", example = "3")
		int eventCount,

		@Schema(description = "해당 날짜의 시술 목록")
		List<ProcedureEventResponseDto> events
) {
	public static CalendarDateResponseDto of(LocalDate date, List<ProcedureEventResponseDto> events) {
		return new CalendarDateResponseDto(date, events.size(), events);
	}
}
