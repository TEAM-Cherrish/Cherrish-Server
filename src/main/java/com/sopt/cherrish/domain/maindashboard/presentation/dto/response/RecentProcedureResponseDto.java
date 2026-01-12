package com.sopt.cherrish.domain.maindashboard.presentation.dto.response;

import java.time.temporal.ChronoUnit;

import com.sopt.cherrish.domain.userprocedure.domain.model.ProcedurePhase;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "최근 시술 정보")
public class RecentProcedureResponseDto {

	@Schema(description = "시술명", example = "레이저 토닝")
	private String name;

	@Schema(description = "시술 후 경과 일수", example = "3")
	private Integer daysSince;

	@Schema(description = "현재 단계", example = "SENSITIVE")
	private ProcedurePhase currentPhase;

	public static RecentProcedureResponseDto from(
		UserProcedure userProcedure,
		java.time.LocalDate today,
		ProcedurePhase phase
	) {
		int daysSince = (int) ChronoUnit.DAYS.between(
			userProcedure.getScheduledAt().toLocalDate(),
			today
		);

		return RecentProcedureResponseDto.builder()
			.name(userProcedure.getProcedure().getName())
			.daysSince(daysSince)
			.currentPhase(phase)
			.build();
	}
}
