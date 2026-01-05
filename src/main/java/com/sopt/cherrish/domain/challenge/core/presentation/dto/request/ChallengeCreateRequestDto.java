package com.sopt.cherrish.domain.challenge.core.presentation.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "챌린지 생성 요청")
public record ChallengeCreateRequestDto(
	@Schema(description = "홈케어 루틴 ID (1-6)", example = "1", requiredMode = RequiredMode.REQUIRED)
	@NotNull(message = "홈케어 루틴 ID는 필수입니다")
	Integer homecareRoutineId,

	@Schema(description = "루틴명 리스트 (1-10개, 각 100자 이하)",
		example = "[\"아침 세안\", \"토너 바르기\", \"크림 바르기\"]",
		requiredMode = RequiredMode.REQUIRED)
	@NotNull(message = "루틴명 리스트는 필수입니다")
	@Size(min = 1, max = 10, message = "루틴은 1개 이상 10개 이하여야 합니다")
	List<@NotBlank(message = "루틴명은 필수입니다")
	@Size(max = 100, message = "루틴명은 100자를 초과할 수 없습니다")
		String> routineNames
) {
}
