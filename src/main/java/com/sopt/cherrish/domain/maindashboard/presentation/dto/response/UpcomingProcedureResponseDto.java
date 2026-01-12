package com.sopt.cherrish.domain.maindashboard.presentation.dto.response;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "날짜별 예정 시술 정보")
public class UpcomingProcedureResponseDto {

	@Schema(description = "시술 날짜", example = "2026-01-20")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@Schema(description = "해당 날짜의 대표 시술명 (다운타임 가장 긴 것)", example = "보톡스")
	private String name;

	@Schema(description = "해당 날짜의 총 시술 개수", example = "2")
	private Integer count;

	@Schema(description = "D-Day (시술까지 남은 일수)", example = "5")
	private Integer dDay;

	public static UpcomingProcedureResponseDto of(
		LocalDate date,
		String name,
		Integer count,
		LocalDate today
	) {
		int dDay = (int) ChronoUnit.DAYS.between(today, date);

		return UpcomingProcedureResponseDto.builder()
			.date(date)
			.name(name)
			.count(count)
			.dDay(dDay)
			.build();
	}
}
