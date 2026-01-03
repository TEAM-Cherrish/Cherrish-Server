package com.sopt.cherrish.domain.challenge.presentation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.challenge.application.service.AiChallengeRecommendationService;
import com.sopt.cherrish.domain.challenge.application.service.HomecareRoutineService;
import com.sopt.cherrish.domain.challenge.presentation.dto.request.AiRecommendationRequestDto;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.AiRecommendationResponseDto;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.HomecareRoutineResponseDto;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
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
	@PostMapping("/ai-recommendations")
	public CommonApiResponse<AiRecommendationResponseDto> generateAiRecommendation(
		@Valid @RequestBody AiRecommendationRequestDto request
	) {
		AiRecommendationResponseDto response = aiRecommendationService.generateRecommendation(request.homecareRoutineId());
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
