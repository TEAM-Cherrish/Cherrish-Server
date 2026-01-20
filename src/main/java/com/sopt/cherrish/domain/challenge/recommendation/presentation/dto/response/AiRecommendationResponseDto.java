package com.sopt.cherrish.domain.challenge.recommendation.presentation.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 챌린지 추천 응답")
public record AiRecommendationResponseDto(
	@Schema(description = "추천 루틴 리스트 (6개)")
	List<String> routines
) {
	public static AiRecommendationResponseDto of(List<String> routines) {
		return new AiRecommendationResponseDto(routines);
	}
}
