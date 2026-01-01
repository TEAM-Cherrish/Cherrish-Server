package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record CalendarDateDto(
		@Schema(description = "날짜", example = "2025-01-15")
		LocalDate date,

		@Schema(description = "해당 날짜의 이벤트 수", example = "3")
		int eventCount,

		@Schema(description = "이벤트 목록")
		List<ProcedureEventDto> events
) {
	public static CalendarDateDto of(LocalDate date, List<ProcedureEventDto> events) {
		return new CalendarDateDto(date, events.size(), events);
	}
}
