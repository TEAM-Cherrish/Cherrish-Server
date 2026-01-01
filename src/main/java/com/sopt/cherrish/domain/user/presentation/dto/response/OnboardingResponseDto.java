package com.sopt.cherrish.domain.user.presentation.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.user.domain.model.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "온보딩 프로필 생성 응답")
public class OnboardingResponseDto {

	@Schema(description = "사용자 고유 식별자", example = "1")
	private Long id;

	@Schema(description = "사용자 이름", example = "홍길동")
	private String name;

	@Schema(description = "가입일시", example = "2024-01-15T10:30:00", type = "string")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime date;

	// Entity -> DTO 변환
	public static OnboardingResponseDto from(User user) {
		return OnboardingResponseDto.builder()
			.id(user.getId())
			.name(user.getName())
			.date(user.getCreatedAt())
			.build();
	}
}
