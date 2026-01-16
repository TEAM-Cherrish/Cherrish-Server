package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.domain.vo.DowntimePeriod;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시술 다운타임 상세")
public record ProcedureEventDowntimeResponseDto(
	@Schema(description = "사용자 시술 일정 ID", example = "123")
	Long userProcedureId,

	@Schema(description = "예약 날짜 및 시간", example = "2026-01-15T14:00:00")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime scheduledAt,

	@Schema(description = "다운타임(일)", example = "7")
	Integer downtimeDays,

	@Schema(description = "민감기 날짜 목록", example = "[\"2026-01-15\", \"2026-01-16\", \"2026-01-17\"]")
	@JsonSerialize(contentUsing = LocalDateSerializer.class)
	List<LocalDate> sensitiveDays,

	@Schema(description = "주의기 날짜 목록", example = "[\"2026-01-18\", \"2026-01-19\"]")
	@JsonSerialize(contentUsing = LocalDateSerializer.class)
	List<LocalDate> cautionDays,

	@Schema(description = "회복기 날짜 목록", example = "[\"2026-01-20\", \"2026-01-21\"]")
	@JsonSerialize(contentUsing = LocalDateSerializer.class)
	List<LocalDate> recoveryDays
) {
	public static ProcedureEventDowntimeResponseDto from(UserProcedure userProcedure) {
		DowntimePeriod downtimePeriod = userProcedure.calculateDowntimePeriod();

		return new ProcedureEventDowntimeResponseDto(
			userProcedure.getId(),
			userProcedure.getScheduledAt(),
			userProcedure.getDowntimeDays(),
			downtimePeriod.getSensitiveDays(),
			downtimePeriod.getCautionDays(),
			downtimePeriod.getRecoveryDays()
		);
	}
}
