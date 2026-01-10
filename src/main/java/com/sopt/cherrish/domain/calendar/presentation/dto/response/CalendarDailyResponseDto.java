package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "일자별 시술 상세 조회 응답")
public class CalendarDailyResponseDto {

	@Schema(description = "이벤트 개수", example = "3")
	private int eventCount;

	@Schema(description = "해당 일자의 시술 이벤트 목록")
	private List<ProcedureEventResponseDto> events;

	public static CalendarDailyResponseDto from(List<ProcedureEventResponseDto> events) {
		return CalendarDailyResponseDto.builder()
			.eventCount(events.size())
			.events(events)
			.build();
	}
}
