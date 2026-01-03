package com.sopt.cherrish.domain.challenge.application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiChallengeRecommendation(
	@JsonProperty("challenge_title")
	String challengeTitle,

	@JsonProperty("routines")
	List<String> routines
) {
}
