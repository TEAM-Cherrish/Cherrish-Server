package com.sopt.cherrish.domain.challenge.presentation.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.challenge.domain.model.ChallengeRoutine;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 루틴 응답")
public record ChallengeRoutineResponseDto(
	@Schema(description = "루틴 ID", example = "1")
	Long routineId,

	@Schema(description = "루틴명", example = "아침 세안")
	String name,

	@Schema(description = "예정일", example = "2025-01-05")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate scheduledDate,

	@Schema(description = "완료 여부", example = "false")
	Boolean isComplete
) {
	public static ChallengeRoutineResponseDto from(ChallengeRoutine routine) {
		return new ChallengeRoutineResponseDto(
			routine.getId(),
			routine.getName(),
			routine.getScheduledDate(),
			routine.getIsComplete()
		);
	}
}
