package com.sopt.cherrish.domain.calendar.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
	private Long id;

	@Schema(description = "시술 ID", example = "5")
	private Long procedureId;

	@Schema(description = "시술명", example = "레이저 토닝")
	private String name;

	@Schema(description = "예약 날짜 및 시간", example = "2025-01-15T14:00:00")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime scheduledAt;

	@Schema(description = "다운타임(일)", example = "7")
	private Integer downtimeDays;

	@Schema(description = "민감기 날짜 목록", example = "[\"2025-01-15\", \"2025-01-16\", \"2025-01-17\"]")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private List<LocalDate> sensitiveDays;

	@Schema(description = "주의기 날짜 목록", example = "[\"2025-01-18\", \"2025-01-19\"]")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private List<LocalDate> cautionDays;

	@Schema(description = "회복기 날짜 목록", example = "[\"2025-01-20\", \"2025-01-21\"]")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private List<LocalDate> recoveryDays;

	public static ProcedureEventResponseDto from(UserProcedure userProcedure) {
		LocalDate scheduledDate = userProcedure.getScheduledAt().toLocalDate();
		Integer downtimeDays = userProcedure.getDowntimeDays();

		// 다운타임이 null이거나 0이면 모든 날짜 목록을 빈 리스트로
		if (downtimeDays == null || downtimeDays == 0) {
			return ProcedureEventResponseDto.builder()
				.type(CalendarEventType.PROCEDURE)
				.id(userProcedure.getId())
				.procedureId(userProcedure.getProcedure().getId())
				.name(userProcedure.getProcedure().getName())
				.scheduledAt(userProcedure.getScheduledAt())
				.downtimeDays(downtimeDays)
				.sensitiveDays(List.of())
				.cautionDays(List.of())
				.recoveryDays(List.of())
				.build();
		}

		// 다운타임을 3으로 나누어 기간 계산
		int baseDays = downtimeDays / 3;
		int remainder = downtimeDays % 3;

		int sensitiveDaysCount = baseDays + (remainder >= 1 ? 1 : 0);
		int cautionDaysCount = baseDays + (remainder >= 2 ? 1 : 0);
		int recoveryDaysCount = baseDays;

		// 날짜 목록 생성
		List<LocalDate> sensitiveDays = generateDateRange(scheduledDate, sensitiveDaysCount);
		List<LocalDate> cautionDays = generateDateRange(scheduledDate.plusDays(sensitiveDaysCount),
			cautionDaysCount);
		List<LocalDate> recoveryDays = generateDateRange(
			scheduledDate.plusDays(sensitiveDaysCount + cautionDaysCount), recoveryDaysCount);

		return ProcedureEventResponseDto.builder()
			.type(CalendarEventType.PROCEDURE)
			.id(userProcedure.getId())
			.procedureId(userProcedure.getProcedure().getId())
			.name(userProcedure.getProcedure().getName())
			.scheduledAt(userProcedure.getScheduledAt())
			.downtimeDays(downtimeDays)
			.sensitiveDays(sensitiveDays)
			.cautionDays(cautionDays)
			.recoveryDays(recoveryDays)
			.build();
	}

	private static List<LocalDate> generateDateRange(LocalDate startDate, int days) {
		if (days <= 0) {
			return List.of();
		}
		return java.util.stream.IntStream.range(0, days)
			.mapToObj(startDate::plusDays)
			.toList();
	}
}
