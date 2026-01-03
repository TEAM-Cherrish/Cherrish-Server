package com.sopt.cherrish.domain.challenge.presentation.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "AI 챌린지 추천 응답")
@Builder
public record AiRecommendationResponseDto(
	@Schema(description = "AI가 생성한 챌린지 제목", example = "피부 보습 7일 챌린지")
	String challengeTitle,

	@Schema(description = "추천 루틴 리스트 (3-5개)")
	List<String> routines
) {
	public static AiRecommendationResponseDto of(String challengeTitle, List<String> routines) {
		return AiRecommendationResponseDto.builder()
			.challengeTitle(challengeTitle)
			.routines(routines)
			.build();
	}
}
