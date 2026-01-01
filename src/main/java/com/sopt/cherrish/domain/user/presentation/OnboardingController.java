package com.sopt.cherrish.domain.user.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.user.application.service.OnboardingService;
import com.sopt.cherrish.domain.user.presentation.dto.request.OnboardingRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.OnboardingResponseDto;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.annotation.AutoApiResponse;
import com.sopt.cherrish.global.annotation.SuccessCodeAnnotation;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@AutoApiResponse
@Tag(name = "Onboarding", description = "온보딩 관련 API")
public class OnboardingController {

	private final OnboardingService onboardingService;

	@Operation(
		summary = "온보딩 프로필 생성",
		description = "새 사용자의 프로필을 생성합니다. 이름과 나이를 입력받아 사용자를 등록합니다."
	)
	@SuccessCodeAnnotation(SuccessCode.SUCCESS)
	@ApiExceptions({ErrorCode.class})
	@PostMapping("/profiles")
	public CommonApiResponse<OnboardingResponseDto> createProfile(
		@Valid @RequestBody OnboardingRequestDto request
	) {
		OnboardingResponseDto response = onboardingService.createProfile(request);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
