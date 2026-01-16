package com.sopt.cherrish.domain.maindashboard.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메인 대시보드 응답")
public record MainDashboardResponseDto(
	@Schema(description = "오늘 날짜", example = "2026-01-15")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate date,

	@Schema(description = "오늘 요일", example = "MONDAY")
	String dayOfWeek,

	@Schema(description = "진행 중인 챌린지 이름 (없으면 null)", example = "7일 보습 챌린지")
	String challengeName,

	@Schema(description = "체리 레벨 (1-4, 챌린지 없으면 0)", example = "2")
	Integer cherryLevel,

	@Schema(description = "챌린지 완료율 (%)", example = "40")
	Integer challengeRate,

	@Schema(description = "최근 시술 목록 (다운타임 진행 중)")
	List<RecentProcedureResponseDto> recentProcedures,

	@Schema(description = "예정된 시술 목록 (날짜별 그룹, 최대 3개 날짜)")
	List<UpcomingProcedureResponseDto> upcomingProcedures
) {
	public static MainDashboardResponseDto from(
		LocalDate today,
		Integer cherryLevel,
		Integer challengeRate,
		String challengeName,
		List<RecentProcedureResponseDto> recentProcedures,
		List<UpcomingProcedureResponseDto> upcomingProcedures
	) {
		return new MainDashboardResponseDto(
			today,
			today.getDayOfWeek().name(),
			challengeName,
			cherryLevel,
			challengeRate,
			recentProcedures,
			upcomingProcedures
		);
	}
}
