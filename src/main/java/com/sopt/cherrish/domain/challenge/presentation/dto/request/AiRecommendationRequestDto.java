package com.sopt.cherrish.domain.challenge.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "AI 챌린지 추천 요청")
public record AiRecommendationRequestDto(
	@Schema(description = "홈케어 루틴 ID", example = "1")
	@NotNull(message = "홈케어 루틴 ID는 필수입니다")
	Long homecareRoutineId
) {
}
