package com.sopt.cherrish.domain.user.presentation;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.user.application.service.UserService;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.presentation.dto.request.UserUpdateRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserResponseDto;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.annotation.AutoApiResponse;
import com.sopt.cherrish.global.annotation.SuccessCodeAnnotation;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@AutoApiResponse
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

	private final UserService userService;

	@Operation(
		summary = "사용자 조회",
		description = "사용자 ID로 사용자 정보를 조회합니다."
	)
	@SuccessCodeAnnotation(SuccessCode.SUCCESS)
	@ApiExceptions({UserErrorCode.class, ErrorCode.class})
	@GetMapping("/{id}")
	public CommonApiResponse<UserResponseDto> getUser(
		@Parameter(description = "사용자 ID", required = true, example = "1")
		@PathVariable Long id
	) {
		UserResponseDto response = userService.getUser(id);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	@Operation(
		summary = "사용자 정보 수정",
		description = "사용자의 이름 또는 나이를 수정합니다. 제공된 필드만 수정됩니다."
	)
	@SuccessCodeAnnotation(SuccessCode.SUCCESS)
	@ApiExceptions({UserErrorCode.class, ErrorCode.class})
	@PatchMapping("/{id}")
	public CommonApiResponse<UserResponseDto> updateUser(
		@Parameter(description = "사용자 ID", required = true, example = "1")
		@PathVariable Long id,
		@Valid @RequestBody UserUpdateRequestDto request
	) {
		UserResponseDto response = userService.updateUser(id, request);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}

	@Operation(
		summary = "사용자 삭제",
		description = "사용자를 삭제합니다."
	)
	@SuccessCodeAnnotation(SuccessCode.SUCCESS)
	@ApiExceptions({UserErrorCode.class, ErrorCode.class})
	@DeleteMapping("/{id}")
	public CommonApiResponse<Void> deleteUser(
		@Parameter(description = "사용자 ID", required = true, example = "1")
		@PathVariable Long id
	) {
		userService.deleteUser(id);
		return CommonApiResponse.success(SuccessCode.SUCCESS);
	}
}
