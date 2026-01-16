package com.sopt.cherrish.domain.maindashboard.presentation.dto.response;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.sopt.cherrish.domain.userprocedure.domain.model.ProcedurePhase;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최근 시술 정보")
public record RecentProcedureResponseDto(
	@Schema(description = "시술명", example = "레이저 토닝")
	String name,

	@Schema(description = "회복 N일차 (시술 당일 = 1일차)", example = "3")
	Integer daysSince,

	@Schema(description = "현재 단계", example = "SENSITIVE")
	ProcedurePhase currentPhase
) {
	public static RecentProcedureResponseDto from(
		UserProcedure userProcedure,
		LocalDate today,
		ProcedurePhase phase
	) {
		int daysSince = (int) ChronoUnit.DAYS.between(
			userProcedure.getScheduledAt().toLocalDate(),
			today
		);

		return new RecentProcedureResponseDto(
			userProcedure.getProcedure().getName(),
			daysSince + 1,  // 회복 N일차 (시술 당일 = 1일차)
			phase
		);
	}
}
