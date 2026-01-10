package com.sopt.cherrish.domain.userprocedure.presentation.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 시술 일정 응답")
public class UserProcedureResponseDto {

	@Schema(description = "사용자 시술 일정 ID", example = "1")
	private Long userProcedureId;

	@Schema(description = "시술 ID", example = "1")
	private Long procedureId;

	@Schema(description = "시술명", example = "레이저 토닝")
	private String procedureName;

	@Schema(description = "예약 날짜 및 시간", example = "2026-01-01T16:00:00", type = "string")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime scheduledAt;

	@Schema(description = "개인 다운타임(일)", example = "6")
	private Integer downtimeDays;

	public static UserProcedureResponseDto from(UserProcedure userProcedure) {
		return UserProcedureResponseDto.builder()
			.userProcedureId(userProcedure.getId())
			.procedureId(userProcedure.getProcedure().getId())
			.procedureName(userProcedure.getProcedure().getName())
			.scheduledAt(userProcedure.getScheduledAt())
			.downtimeDays(userProcedure.getDowntimeDays())
			.build();
	}
}
