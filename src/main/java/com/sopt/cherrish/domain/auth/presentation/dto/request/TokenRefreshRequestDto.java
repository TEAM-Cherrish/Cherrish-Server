package com.sopt.cherrish.domain.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record TokenRefreshRequestDto(

	@Schema(description = "Refresh Token",
		example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
		requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Refresh Token은 필수입니다")
	String refreshToken
) {
}
