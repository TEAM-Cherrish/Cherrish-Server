package com.sopt.cherrish.domain.challenge.recommendation.infrastructure.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenAI API 응답 구조
 * Infrastructure 계층의 외부 API 응답 DTO
 */
public record OpenAiChallengeRecommendationResponseDto(
	@JsonProperty("routines")
	List<String> routines
) {
}
