package com.sopt.cherrish.domain.challenge.recommendation.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.ai.exception.AiErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.recommendation.application.service.AiChallengeRecommendationService;
import com.sopt.cherrish.domain.challenge.recommendation.presentation.dto.request.AiRecommendationRequestDto;
import com.sopt.cherrish.domain.challenge.recommendation.presentation.dto.response.AiRecommendationResponseDto;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenge Recommendation", description = "챌린지 AI 추천 관련 API")
public class ChallengeRecommendationController {

	private final AiChallengeRecommendationService aiRecommendationService;

	@Operation(
		summary = "AI 챌린지 추천",
		description = "홈케어 루틴 ID를 입력받아 AI가 맞춤 챌린지 타이틀과 루틴 리스트를 생성합니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, AiErrorCode.class, ErrorCode.class})
	@PostMapping("/ai-recommendations")
	public CommonApiResponse<AiRecommendationResponseDto> generateAiRecommendation(
		@Valid @RequestBody AiRecommendationRequestDto request
	) {
		AiRecommendationResponseDto response = aiRecommendationService.generateRecommendation(
			request.homecareRoutineId());
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
