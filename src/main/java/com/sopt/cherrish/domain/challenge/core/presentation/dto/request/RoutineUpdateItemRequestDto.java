package com.sopt.cherrish.domain.challenge.core.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "루틴 업데이트 항목")
public record RoutineUpdateItemRequestDto(
	@Schema(description = "루틴 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "루틴 ID는 필수입니다")
	Long routineId,

	@Schema(description = "완료 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "완료 여부는 필수입니다")
	Boolean isComplete
) {
}
