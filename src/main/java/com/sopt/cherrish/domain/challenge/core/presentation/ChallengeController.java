package com.sopt.cherrish.domain.challenge.core.presentation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.ai.exception.AiErrorCode;
import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeCreationFacade;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.homecare.application.service.HomecareRoutineService;
import com.sopt.cherrish.domain.challenge.homecare.presentation.dto.response.HomecareRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.recommendation.application.service.AiChallengeRecommendationService;
import com.sopt.cherrish.domain.challenge.recommendation.presentation.dto.request.AiRecommendationRequestDto;
import com.sopt.cherrish.domain.challenge.recommendation.presentation.dto.response.AiRecommendationResponseDto;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenge", description = "챌린지 관련 API")
public class ChallengeController {

	private final HomecareRoutineService homecareRoutineService;
	private final AiChallengeRecommendationService aiRecommendationService;
	private final ChallengeCreationFacade challengeCreationFacade;

	@Operation(
		summary = "홈케어 루틴 목록 조회",
		description = "사용 가능한 모든 홈케어 루틴 카테고리 목록을 조회합니다."
	)
	@GetMapping("/homecare-routines")
	public CommonApiResponse<List<HomecareRoutineResponseDto>> getHomecareRoutines() {
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();
		return CommonApiResponse.success(SuccessCode.SUCCESS, routines);
	}

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

	@Operation(
		summary = "챌린지 생성",
		description = "홈케어 루틴 ID와 루틴명 리스트를 입력받아 7일 챌린지를 생성합니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@PostMapping("/{userId}")
	public CommonApiResponse<ChallengeCreateResponseDto> createChallenge(
		@Parameter(description = "사용자 ID", required = true, example = "1")
		@PathVariable Long userId,
		@Valid @RequestBody ChallengeCreateRequestDto request
	) {
		ChallengeCreateResponseDto response = challengeCreationFacade.createChallenge(userId, request);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
