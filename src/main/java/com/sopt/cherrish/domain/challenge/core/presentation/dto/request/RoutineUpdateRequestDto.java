package com.sopt.cherrish.domain.challenge.core.presentation.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RoutineUpdateRequestDto(
	@NotEmpty(message = "업데이트할 루틴 목록은 비어 있을 수 없습니다")
	@Size(max = 20, message = "업데이트할 루틴은 최대 20개까지 가능합니다")
	@Valid
	@Schema(
		description = "업데이트할 루틴 목록",
		requiredMode = Schema.RequiredMode.REQUIRED,
		example = "[{\"routineId\": 1, \"isComplete\": true}, {\"routineId\": 2, \"isComplete\": false}]"
	)
	List<RoutineUpdateItemRequestDto> routines
) {
}
