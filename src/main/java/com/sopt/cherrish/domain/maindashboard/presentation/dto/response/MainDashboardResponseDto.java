package com.sopt.cherrish.domain.maindashboard.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "메인 대시보드 응답")
public class MainDashboardResponseDto {

	@Schema(description = "오늘 날짜", example = "2026-01-15")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@Schema(description = "오늘 요일", example = "MONDAY")
	private String dayOfWeek;

	@Schema(description = "진행 중인 챌린지 이름 (없으면 null)", example = "7일 보습 챌린지")
	private String challengeName;

	@Schema(description = "체리 레벨 (1-4, 챌린지 없으면 0)", example = "2")
	private Integer cherryLevel;

	@Schema(description = "챌린지 완료율 (%)", example = "40.3")
	private Double challengeRate;

	@Schema(description = "최근 시술 목록 (다운타임 진행 중)")
	private List<RecentProcedureResponseDto> recentProcedures;

	@Schema(description = "예정된 시술 목록 (날짜별 그룹, 최대 3개 날짜)")
	private List<UpcomingProcedureResponseDto> upcomingProcedures;

	public static MainDashboardResponseDto from(
		LocalDate today,
		Integer cherryLevel,
		Double challengeRate,
		String challengeName,
		List<RecentProcedureResponseDto> recentProcedures,
		List<UpcomingProcedureResponseDto> upcomingProcedures
	) {
		return MainDashboardResponseDto.builder()
			.date(today)
			.dayOfWeek(today.getDayOfWeek().name())
			.challengeName(challengeName)
			.cherryLevel(cherryLevel)
			.challengeRate(challengeRate)
			.recentProcedures(recentProcedures)
			.upcomingProcedures(upcomingProcedures)
			.build();
	}
}
