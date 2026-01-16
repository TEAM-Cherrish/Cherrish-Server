package com.sopt.cherrish.domain.user.presentation.dto.response;

import com.sopt.cherrish.domain.user.domain.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 요약 정보 응답")
public record UserSummaryResponseDto(
	@Schema(description = "사용자 이름", example = "홍길동")
	String name,

	@Schema(description = "회원가입 경과일 (가입 당일 = 1일차)", example = "3")
	Integer daysSinceSignup
) {
	public static UserSummaryResponseDto from(User user, int daysSinceSignup) {
		return new UserSummaryResponseDto(
			user.getName(),
			daysSinceSignup
		);
	}
}
