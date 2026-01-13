package com.sopt.cherrish.domain.challenge.core.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeCreationFacade;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.core.response.success.ChallengeSuccessCode;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenge - Lifecycle", description = "챌린지 생명주기 관리 API")
public class ChallengeLifecycleController {

	private final ChallengeCreationFacade challengeCreationFacade;

	// TODO: Spring Security 추가 시 RequestHeader userId 제거하고 @AuthenticationPrincipal 사용
	@Operation(
		summary = "챌린지 생성",
		description = "홈케어 루틴 ID와 루틴명 리스트를 입력받아 7일 챌린지를 생성합니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@PostMapping
	public CommonApiResponse<ChallengeCreateResponseDto> createChallenge(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody ChallengeCreateRequestDto request
	) {
		ChallengeCreateResponseDto response = challengeCreationFacade.createChallenge(userId, request);
		return CommonApiResponse.success(ChallengeSuccessCode.CHALLENGE_CREATED, response);
	}
}
