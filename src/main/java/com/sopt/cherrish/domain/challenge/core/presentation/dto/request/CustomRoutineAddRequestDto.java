package com.sopt.cherrish.domain.challenge.core.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "커스텀 루틴 추가 요청")
public record CustomRoutineAddRequestDto(
	@Schema(description = "루틴명", example = "저녁 마사지", requiredMode = RequiredMode.REQUIRED)
	@NotBlank(message = "루틴명은 필수입니다")
	@Size(max = 100, message = "루틴명은 100자를 초과할 수 없습니다")
	String routineName
) {
}
