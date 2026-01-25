package com.sopt.cherrish.domain.auth.presentation.dto.request;

import com.sopt.cherrish.domain.auth.domain.model.SocialProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "소셜 로그인 요청")
public record SocialLoginRequestDto(

	@Schema(description = "소셜 로그인 제공자", example = "KAKAO", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "소셜 로그인 제공자는 필수입니다")
	SocialProvider provider,

	@Schema(description = "소셜 토큰 (카카오: Access Token, 애플: Identity Token)",
		example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
		requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "소셜 토큰은 필수입니다")
	String token
) {
}
