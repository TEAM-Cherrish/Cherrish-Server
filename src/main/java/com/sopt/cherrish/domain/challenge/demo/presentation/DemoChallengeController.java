package com.sopt.cherrish.domain.challenge.demo.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;
import com.sopt.cherrish.domain.challenge.demo.application.facade.DemoChallengeAdvanceDayFacade;
import com.sopt.cherrish.domain.challenge.demo.application.facade.DemoChallengeCreationFacade;
import com.sopt.cherrish.domain.challenge.demo.application.facade.DemoChallengeQueryFacade;
import com.sopt.cherrish.domain.challenge.demo.application.service.DemoChallengeRoutineService;
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
@RequestMapping("/api/demo/challenges")
@RequiredArgsConstructor
@Tag(name = "Demo Challenge", description = "데모 전용 챌린지 API (날짜 진행 테스트용)")
public class DemoChallengeController {

	private final DemoChallengeCreationFacade creationFacade;
	private final DemoChallengeQueryFacade queryFacade;
	private final DemoChallengeAdvanceDayFacade advanceDayFacade;
	private final DemoChallengeRoutineService routineService;

	@Operation(
		summary = "데모 챌린지 생성",
		description = "데모용 7일 챌린지를 생성합니다. 가상 날짜로 시작일이 설정됩니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@PostMapping
	public CommonApiResponse<ChallengeCreateResponseDto> createChallenge(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody ChallengeCreateRequestDto request
	) {
		ChallengeCreateResponseDto response = creationFacade.createChallenge(userId, request);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	@Operation(
		summary = "데모 챌린지 조회",
		description = "활성 데모 챌린지와 진행 상황을 조회합니다. 가상 날짜 기준으로 조회됩니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@GetMapping
	public CommonApiResponse<ChallengeDetailResponseDto> getActiveChallenge(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId
	) {
		ChallengeDetailResponseDto response = queryFacade.getActiveChallengeDetail(userId);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	@Operation(
		summary = "다음 날로 넘어가기 (데모용)",
		description = "가상 날짜를 +1일 진행하고 통계를 재계산합니다. 새 날짜의 루틴 목록을 반환합니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@PostMapping("/{demoChallengeId}/advance-day")
	public CommonApiResponse<ChallengeDetailResponseDto> advanceDay(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,

		@Parameter(description = "데모 챌린지 ID", required = true, example = "1")
		@PathVariable Long demoChallengeId
	) {
		ChallengeDetailResponseDto response = advanceDayFacade.advanceDay(userId, demoChallengeId);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	@Operation(
		summary = "데모 루틴 완료 토글",
		description = "루틴 완료 상태를 토글합니다. 통계는 즉시 업데이트되지 않고 '다음 날로 넘어가기' 시 반영됩니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@PatchMapping("/routines/{routineId}/toggle")
	public CommonApiResponse<RoutineCompletionResponseDto> toggleRoutine(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,

		@Parameter(description = "루틴 ID", required = true, example = "1")
		@PathVariable Long routineId
	) {
		RoutineCompletionResponseDto response = routineService.toggleCompletion(userId, routineId);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
