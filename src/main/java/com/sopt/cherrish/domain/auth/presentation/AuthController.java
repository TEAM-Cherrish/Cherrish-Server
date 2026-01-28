package com.sopt.cherrish.domain.auth.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.auth.application.service.AuthService;
import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;
import com.sopt.cherrish.domain.auth.presentation.dto.request.SocialLoginRequestDto;
import com.sopt.cherrish.domain.auth.presentation.dto.request.TokenRefreshRequestDto;
import com.sopt.cherrish.domain.auth.presentation.dto.response.LoginResponseDto;
import com.sopt.cherrish.domain.auth.presentation.dto.response.TokenResponseDto;
import com.sopt.cherrish.domain.auth.response.success.AuthSuccessCode;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.security.CurrentUser;
import com.sopt.cherrish.global.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

	private final AuthService authService;

	@Operation(
		summary = "소셜 로그인",
		description = "카카오 또는 애플 소셜 토큰으로 로그인합니다. 신규 사용자는 isNewUser: true를 반환합니다."
	)
	@SecurityRequirements
	@ApiExceptions({AuthErrorCode.class, ErrorCode.class})
	@PostMapping("/login")
	public CommonApiResponse<LoginResponseDto> login(
		@Valid @RequestBody SocialLoginRequestDto request
	) {
		LoginResponseDto response = authService.login(request);
		return CommonApiResponse.success(AuthSuccessCode.LOGIN_SUCCESS, response);
	}

	@Operation(
		summary = "토큰 재발급",
		description = "Refresh Token으로 새로운 Access Token과 Refresh Token을 발급받습니다."
	)
	@SecurityRequirements
	@ApiExceptions({AuthErrorCode.class, ErrorCode.class})
	@PostMapping("/refresh")
	public CommonApiResponse<TokenResponseDto> refresh(
		@Valid @RequestBody TokenRefreshRequestDto request
	) {
		TokenResponseDto response = authService.refresh(request);
		return CommonApiResponse.success(AuthSuccessCode.TOKEN_REFRESHED, response);
	}

	@Operation(
		summary = "로그아웃",
		description = "현재 사용자의 Refresh Token을 무효화하고 Access Token을 블랙리스트에 추가합니다."
	)
	@ApiExceptions({AuthErrorCode.class, ErrorCode.class})
	@PostMapping("/logout")
	public CommonApiResponse<Void> logout(
		@CurrentUser UserPrincipal userPrincipal,
		@Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader
	) {
		authService.logout(userPrincipal.getUserId(), authorizationHeader);
		return CommonApiResponse.success(AuthSuccessCode.LOGOUT_SUCCESS);
	}
}
