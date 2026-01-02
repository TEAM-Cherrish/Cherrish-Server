package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import com.sopt.cherrish.domain.calendar.domain.model.UserProcedure;
import com.sopt.cherrish.domain.calendar.domain.vo.DowntimePeriods;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProcedureEventDto(
		@Schema(description = "이벤트 타입", example = "PROCEDURE")
        @NotBlank(message = "타입 정보가 누락되었습니다.")
		String type,

		@Schema(description = "사용자 시술 일정 ID", example = "123")
        @NotNull(message = "ID는 필수입니다.")
		Long id,

		@Schema(description = "시술 ID", example = "5")
        @NotNull(message = "시술 ID는 필수입니다.")
		Long procedureId,

		@Schema(description = "시술명", example = "레이저 토닝")
        @NotBlank(message = "시술명은 필수입니다.")
		String name,

		@Schema(description = "예약 날짜 및 시간", example = "2025-01-15T14:00:00")
        @NotNull(message = "예약 시간 정보는 필수입니다.")
        LocalDateTime scheduledAt,

		@Schema(description = "다운타임 일수", example = "7")
        @NotNull(message = "다운타임 일수 정보가 필요합니다.")
        @Min(value = 0, message = "다운타임은 0 이상이어야 합니다.")
		Integer downtimeDays,

		@Schema(
				description = "민감 기간 날짜 목록",
				example = "[\"2025-01-15\", \"2025-01-16\", \"2025-01-17\"]"
		)
        @NotNull(message = "민감 기간 목록은 필수입니다.")
        List<LocalDate> sensitiveDays,

		@Schema(
				description = "주의 기간 날짜 목록",
				example = "[\"2025-01-18\", \"2025-01-19\"]"
		)
        @NotNull(message = "주의 기간 목록은 필수입니다.")
		List<LocalDate> cautionDays,

		@Schema(
				description = "회복 기간 날짜 목록",
				example = "[\"2025-01-20\", \"2025-01-21\"]"
		)
        @NotNull(message = "회복 기간 목록은 필수입니다.")
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
