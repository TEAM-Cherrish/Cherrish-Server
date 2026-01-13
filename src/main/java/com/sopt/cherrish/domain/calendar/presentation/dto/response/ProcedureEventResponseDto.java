package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.calendar.domain.model.CalendarEventType;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "캘린더 시술 이벤트")
public class ProcedureEventResponseDto {

	@Schema(description = "이벤트 타입", example = "PROCEDURE")
	private CalendarEventType type;

	@Schema(description = "사용자 시술 일정 ID", example = "123")
	private Long userProcedureId;

	@Schema(description = "시술 ID", example = "5")
	private Long procedureId;

	@Schema(description = "시술명", example = "레이저 토닝")
	private String name;

	@Schema(description = "예약 날짜 및 시간", example = "2026-01-15T14:00:00")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime scheduledAt;

	@Schema(description = "다운타임(일)", example = "7")
	private Integer downtimeDays;

	public static ProcedureEventResponseDto from(UserProcedure userProcedure) {
		return ProcedureEventResponseDto.builder()
			.type(CalendarEventType.PROCEDURE)
			.userProcedureId(userProcedure.getId())
			.procedureId(userProcedure.getProcedure().getId())
			.name(userProcedure.getProcedure().getName())
			.scheduledAt(userProcedure.getScheduledAt())
			.downtimeDays(userProcedure.getDowntimeDays())
			.build();
	}
}
