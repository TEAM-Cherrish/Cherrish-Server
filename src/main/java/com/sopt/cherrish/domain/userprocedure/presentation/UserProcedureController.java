package com.sopt.cherrish.domain.userprocedure.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.userprocedure.application.service.UserProcedureService;
import com.sopt.cherrish.domain.userprocedure.exception.UserProcedureErrorCode;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureCreateResponseDto;
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
@RequestMapping("/api/user-procedures")
@RequiredArgsConstructor
@Tag(name = "UserProcedure", description = "사용자 시술 일정 API")
public class UserProcedureController {

	private final UserProcedureService userProcedureService;

	@Operation(
		summary = "시술 일정 추가",
		description = "예약 날짜와 시술 목록을 입력받아 시술 일정을 등록합니다."
	)
	@ApiExceptions({UserProcedureErrorCode.class, ProcedureErrorCode.class, UserErrorCode.class, ErrorCode.class})
	@PostMapping
	public CommonApiResponse<UserProcedureCreateResponseDto> createUserProcedures(
		@Parameter(description = "사용자 ID (X-User-Id 헤더)", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody UserProcedureCreateRequestDto request
	) {
		UserProcedureCreateResponseDto response = userProcedureService.createUserProcedures(userId, request);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

}
