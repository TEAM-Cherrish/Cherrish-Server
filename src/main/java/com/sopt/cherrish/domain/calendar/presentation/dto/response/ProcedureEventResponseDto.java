package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.calendar.domain.model.CalendarEventType;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.domain.vo.DowntimePeriod;

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
	private Long id;

	@Schema(description = "시술 ID", example = "5")
	private Long procedureId;

	@Schema(description = "시술명", example = "레이저 토닝")
	private String name;

	@Schema(description = "예약 날짜 및 시간", example = "2026-01-15T14:00:00")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime scheduledAt;

	@Schema(description = "다운타임(일)", example = "7")
	private Integer downtimeDays;

	@Schema(description = "민감기 날짜 목록", example = "[\"2026-01-15\", \"2026-01-16\", \"2026-01-17\"]")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private List<LocalDate> sensitiveDays;

	@Schema(description = "주의기 날짜 목록", example = "[\"2026-01-18\", \"2026-01-19\"]")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private List<LocalDate> cautionDays;

	@Schema(description = "회복기 날짜 목록", example = "[\"2026-01-20\", \"2026-01-21\"]")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private List<LocalDate> recoveryDays;

	public static ProcedureEventResponseDto from(UserProcedure userProcedure) {
		// Entity로부터 다운타임 기간 계산 (비즈니스 로직은 Entity에서 처리)
		DowntimePeriod downtimePeriod = userProcedure.calculateDowntimePeriod();

		return ProcedureEventResponseDto.builder()
			.type(CalendarEventType.PROCEDURE)
			.id(userProcedure.getId())
			.procedureId(userProcedure.getProcedure().getId())
			.name(userProcedure.getProcedure().getName())
			.scheduledAt(userProcedure.getScheduledAt())
			.downtimeDays(userProcedure.getDowntimeDays())
			.sensitiveDays(downtimePeriod.getSensitiveDays())
			.cautionDays(downtimePeriod.getCautionDays())
			.recoveryDays(downtimePeriod.getRecoveryDays())
			.build();
	}
}
