package com.sopt.cherrish.domain.userprocedure.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 시술 일정 응답")
public record UserProcedureResponseDto(
	@Schema(description = "사용자 시술 일정 ID", example = "1")
	Long userProcedureId,

	@Schema(description = "시술 ID", example = "1")
	Long procedureId,

	@Schema(description = "시술명", example = "레이저 토닝")
	String procedureName,

	@Schema(description = "예약 날짜 및 시간", example = "2026-01-01T16:00:00", type = "string")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime scheduledAt,

	@Schema(description = "개인 다운타임(일)", example = "6")
	Integer downtimeDays,

	@Schema(description = "회복 목표일", example = "2026-01-10")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate recoveryTargetDate
) {
	public static UserProcedureResponseDto from(UserProcedure userProcedure) {
		return new UserProcedureResponseDto(
			userProcedure.getId(),
			userProcedure.getProcedure().getId(),
			userProcedure.getProcedure().getName(),
			userProcedure.getScheduledAt(),
			userProcedure.getDowntimeDays(),
			userProcedure.getRecoveryTargetDate()
		);
	}
}
