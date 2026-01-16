package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.calendar.domain.model.CalendarEventType;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "캘린더 시술 이벤트")
public record ProcedureEventResponseDto(
	@Schema(description = "이벤트 타입", example = "PROCEDURE")
	CalendarEventType type,

	@Schema(description = "사용자 시술 일정 ID", example = "123")
	Long userProcedureId,

	@Schema(description = "시술 ID", example = "5")
	Long procedureId,

	@Schema(description = "시술명", example = "레이저 토닝")
	String name,

	@Schema(description = "예약 날짜 및 시간", example = "2026-01-15T14:00:00")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime scheduledAt,

	@Schema(description = "다운타임(일)", example = "7")
	Integer downtimeDays
) {
	public static ProcedureEventResponseDto from(UserProcedure userProcedure) {
		return new ProcedureEventResponseDto(
			CalendarEventType.PROCEDURE,
			userProcedure.getId(),
			userProcedure.getProcedure().getId(),
			userProcedure.getProcedure().getName(),
			userProcedure.getScheduledAt(),
			userProcedure.getDowntimeDays()
		);
	}
}
