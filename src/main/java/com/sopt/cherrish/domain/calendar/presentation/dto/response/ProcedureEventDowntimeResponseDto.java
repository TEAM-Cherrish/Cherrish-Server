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
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "시술 다운타임 상세")
public class ProcedureEventDowntimeResponseDto {

	@Schema(description = "사용자 시술 일정 ID", example = "123")
	private Long userProcedureId;

	@Schema(description = "예약 날짜 및 시간", example = "2026-01-15T14:00:00")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime scheduledAt;

	@Schema(description = "다운타임(일)", example = "7")
	private Integer downtimeDays;

	@Schema(description = "민감기 날짜 목록", example = "[\"2026-01-15\", \"2026-01-16\", \"2026-01-17\"]")
	@JsonSerialize(contentUsing = LocalDateSerializer.class)
	private List<LocalDate> sensitiveDays;

	@Schema(description = "주의기 날짜 목록", example = "[\"2026-01-18\", \"2026-01-19\"]")
	@JsonSerialize(contentUsing = LocalDateSerializer.class)
	private List<LocalDate> cautionDays;

	@Schema(description = "회복기 날짜 목록", example = "[\"2026-01-20\", \"2026-01-21\"]")
	@JsonSerialize(contentUsing = LocalDateSerializer.class)
	private List<LocalDate> recoveryDays;

	public static ProcedureEventDowntimeResponseDto from(UserProcedure userProcedure) {
		// Entity로부터 다운타임 기간 계산
		DowntimePeriod downtimePeriod = userProcedure.calculateDowntimePeriod();

		return ProcedureEventDowntimeResponseDto.builder()
			.userProcedureId(userProcedure.getId())
			.scheduledAt(userProcedure.getScheduledAt())
			.downtimeDays(userProcedure.getDowntimeDays())
			.sensitiveDays(downtimePeriod.getSensitiveDays())
			.cautionDays(downtimePeriod.getCautionDays())
			.recoveryDays(downtimePeriod.getRecoveryDays())
			.build();
	}
}
