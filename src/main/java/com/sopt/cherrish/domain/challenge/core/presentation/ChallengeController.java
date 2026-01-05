package com.sopt.cherrish.domain.challenge.core.presentation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeCreationFacade;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
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

	private final ChallengeCreationFacade challengeCreationFacade;

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
