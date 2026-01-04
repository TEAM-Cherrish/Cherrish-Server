package com.sopt.cherrish.domain.challenge.fixture;

import java.util.Arrays;
import java.util.List;

import com.sopt.cherrish.domain.challenge.application.dto.response.HomecareRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.challenge.presentation.dto.request.AiRecommendationRequestDto;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.AiRecommendationResponseDto;

public class ChallengeTestFixture {

	public static List<HomecareRoutineResponseDto> homecareRoutineList() {
		return Arrays.stream(HomecareRoutine.values())
			.map(HomecareRoutineResponseDto::from)
			.toList();
	}

	// AiRecommendationRequestDto Fixtures
	public static AiRecommendationRequestDto recommendationRequest(Long homecareRoutineId) {
		return new AiRecommendationRequestDto(homecareRoutineId);
	}

	// AiRecommendationResponseDto Fixtures
	public static AiRecommendationResponseDto skinMoisturizingRecommendation() {
		return AiRecommendationResponseDto.of(
			"피부 보습 7일 챌린지",
			List.of("아침 세안 후 토너 바르기", "저녁 보습 크림 바르기", "하루 8잔 물 마시기")
		);
	}

	public static AiRecommendationResponseDto wrinkleCareRecommendation() {
		return AiRecommendationResponseDto.of(
			"주름 개선 7일 챌린지",
			List.of("레티놀 세럼 바르기", "충분한 수면")
		);
	}

	public static AiRecommendationResponseDto emptyRoutinesRecommendation() {
		return AiRecommendationResponseDto.of("테스트 챌린지", List.of());
	}

}
