package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import com.sopt.cherrish.domain.calendar.domain.model.UserProcedure;
import com.sopt.cherrish.domain.calendar.domain.vo.DowntimePeriods;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProcedureEventDto(
		@Schema(description = "이벤트 타입", example = "PROCEDURE")
		String type,

		@Schema(description = "사용자 시술 일정 ID", example = "123")
		Long id,

		@Schema(description = "시술 ID", example = "5")
		Long procedureId,

		@Schema(description = "시술명", example = "레이저 토닝")
		String name,

		@Schema(description = "예약 날짜 및 시간", example = "2025-01-15T14:00:00")
		LocalDateTime scheduledAt,

		@Schema(description = "다운타임 일수", example = "7")
		Integer downtimeDays,

		@Schema(
				description = "민감 기간 날짜 목록",
				example = "[\"2025-01-15\", \"2025-01-16\", \"2025-01-17\"]"
		)
		List<LocalDate> sensitiveDays,

		@Schema(
				description = "주의 기간 날짜 목록",
				example = "[\"2025-01-18\", \"2025-01-19\"]"
		)
		List<LocalDate> cautionDays,

		@Schema(
				description = "회복 기간 날짜 목록",
				example = "[\"2025-01-20\", \"2025-01-21\"]"
		)
		List<LocalDate> recoveryDays
) {
	public static ProcedureEventDto from(UserProcedure userProcedure, DowntimePeriods periods, Integer downtimeDays) {
		return new ProcedureEventDto(
				"PROCEDURE",
				userProcedure.getId(),
				userProcedure.getProcedure().getId(),
				userProcedure.getProcedure().getName(),
				userProcedure.getScheduledAt(),
				downtimeDays,
				periods.sensitiveDays(),
				periods.cautionDays(),
				periods.recoveryDays()
		);
	}
}
