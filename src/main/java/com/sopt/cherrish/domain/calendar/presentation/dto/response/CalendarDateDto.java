package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CalendarDateDto(
		@Schema(description = "날짜", example = "2025-01-15")
        @NotNull(message = "날짜 정보는 필수입니다.")
		LocalDate date,

		@Schema(description = "해당 날짜의 시술 수", example = "3")
        @Min(value = 0, message = "시술 수는 0 이상이어야 합니다.")
		int eventCount,

		@Schema(description = "해당 날짜의 시술 목록")
        @NotNull(message = "시술 목록 필드는 필수입니다.")
		List<ProcedureEventDto> events
) {
	public static CalendarDateDto of(LocalDate date, List<ProcedureEventDto> events) {
		return new CalendarDateDto(date, events.size(), events);
	}
}
