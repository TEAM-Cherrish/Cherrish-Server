package com.sopt.cherrish.domain.challenge.core.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeCreationFacade;
import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeCustomRoutineFacade;
import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeQueryFacade;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.CustomRoutineAddRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.CustomRoutineAddResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineBatchUpdateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;
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
	private final ChallengeQueryFacade challengeQueryFacade;
	private final ChallengeRoutineService challengeRoutineService;
	private final ChallengeCustomRoutineFacade challengeCustomRoutineFacade;

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
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

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
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	// TODO: Spring Security 추가 시 RequestHeader userId 제거하고 @AuthenticationPrincipal 사용
	@Operation(
		summary = "루틴 완료 토글",
		description = "루틴의 완료 상태를 토글합니다. 완료 시 통계와 체리 레벨이 자동 업데이트됩니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@PatchMapping("/routines/{routineId}")
	public CommonApiResponse<RoutineCompletionResponseDto> toggleRoutineCompletion(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Parameter(description = "루틴 ID", required = true, example = "1")
		@PathVariable Long routineId
	) {
		RoutineCompletionResponseDto response =
			challengeRoutineService.toggleCompletion(userId, routineId);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	// TODO: Spring Security 추가 시 RequestHeader userId 제거하고 @AuthenticationPrincipal 사용
	@Operation(
		summary = "루틴 일괄 업데이트",
		description = "여러 루틴의 완료 상태를 한 번에 업데이트합니다. "
			+ "All or Nothing 방식으로 동작하여 하나라도 실패하면 전체 롤백됩니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@PatchMapping("/routines")
	public CommonApiResponse<RoutineBatchUpdateResponseDto> updateMultipleRoutines(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody RoutineUpdateRequestDto request
	) {
		RoutineBatchUpdateResponseDto response = challengeRoutineService.updateMultipleRoutines(userId, request);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	// TODO: Spring Security 추가 시 RequestHeader userId 제거하고 @AuthenticationPrincipal 사용
	@Operation(
		summary = "커스텀 루틴 추가",
		description = "활성 챌린지에 커스텀 루틴을 추가합니다. 오늘부터 챌린지 종료일까지 매일 루틴이 추가되며, 통계가 자동 업데이트됩니다."
	)
	@ApiExceptions({ChallengeErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@PostMapping("/{challengeId}/routines")
	public CommonApiResponse<CustomRoutineAddResponseDto> addCustomRoutine(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Parameter(description = "챌린지 ID", required = true, example = "1")
		@PathVariable Long challengeId,
		@Valid @RequestBody CustomRoutineAddRequestDto request
	) {
		CustomRoutineAddResponseDto response = challengeCustomRoutineFacade.addCustomRoutine(
			userId, challengeId, request
		);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
