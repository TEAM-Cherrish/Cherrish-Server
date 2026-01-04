package com.sopt.cherrish.domain.challenge.application.dto.response;

import com.sopt.cherrish.domain.challenge.domain.model.HomecareRoutine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "홈케어 루틴 응답")
@Builder
public record HomecareRoutineResponseDto(
	@Schema(description = "홈케어 루틴 ID", example = "1")
	int id,

	@Schema(description = "홈케어 루틴 이름 (enum)", example = "SKIN_MOISTURIZING")
	String name,

	@Schema(description = "홈케어 루틴 설명", example = "피부 보습 관리")
	String description
) {
	public static HomecareRoutineResponseDto from(HomecareRoutine routine) {
		return HomecareRoutineResponseDto.builder()
			.id(routine.getId())
			.name(routine.name())
			.description(routine.getDescription())
			.build();
	}
}
