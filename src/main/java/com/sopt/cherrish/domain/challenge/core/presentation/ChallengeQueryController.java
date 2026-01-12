package com.sopt.cherrish.domain.challenge.core.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeQueryFacade;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.core.response.success.ChallengeSuccessCode;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenge - Query", description = "챌린지 조회 API")
public class ChallengeQueryController {

	private final ChallengeQueryFacade challengeQueryFacade;

	// TODO: Spring Security 추가 시 RequestHeader userId 제거하고 @AuthenticationPrincipal 사용
	@Operation(
		summary = "활성 챌린지 조회",
		description = "사용자의 활성 챌린지와 진행 상황, 오늘의 루틴을 조회합니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@GetMapping
	public CommonApiResponse<ChallengeDetailResponseDto> getActiveChallenge(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId
	) {
		ChallengeDetailResponseDto response = challengeQueryFacade.getActiveChallengeDetail(userId);
		return CommonApiResponse.success(ChallengeSuccessCode.CHALLENGE_RETRIEVED, response);
	}
}
