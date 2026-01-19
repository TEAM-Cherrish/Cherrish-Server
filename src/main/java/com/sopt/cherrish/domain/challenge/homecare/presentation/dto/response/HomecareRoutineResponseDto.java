package com.sopt.cherrish.domain.challenge.homecare.presentation.dto.response;

import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "홈케어 루틴 응답")
public record HomecareRoutineResponseDto(
	@Schema(description = "홈케어 루틴 ID", example = "1")
	int id,

	@Schema(description = "홈케어 루틴 이름 (enum)", example = "SKIN_CONDITION")
	String name,

	@Schema(description = "홈케어 루틴 설명", example = "피부 컨디션")
	String description
) {
	public static HomecareRoutineResponseDto from(HomecareRoutine routine) {
		return new HomecareRoutineResponseDto(
			routine.getId(),
			routine.name(),
			routine.getDescription()
		);
	}
}
