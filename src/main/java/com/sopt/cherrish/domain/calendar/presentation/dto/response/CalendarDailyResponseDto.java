package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일자별 시술 상세 조회 응답")
public record CalendarDailyResponseDto(
	@Schema(description = "이벤트 개수", example = "3")
	int eventCount,

	@Schema(description = "해당 일자의 시술 이벤트 목록")
	List<ProcedureEventResponseDto> events
) {
	public static CalendarDailyResponseDto from(List<ProcedureEventResponseDto> events) {
		return new CalendarDailyResponseDto(events.size(), events);
	}
}
