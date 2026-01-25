package com.sopt.cherrish.domain.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponseDto(

	@Schema(description = "사용자 ID", example = "1")
	Long userId,

	@Schema(description = "신규 사용자 여부 (true인 경우 온보딩 필요)", example = "false")
	boolean isNewUser,

	@Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	String accessToken,

	@Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	String refreshToken
) {
}
