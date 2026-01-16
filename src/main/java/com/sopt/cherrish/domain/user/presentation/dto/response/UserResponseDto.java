package com.sopt.cherrish.domain.user.presentation.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.user.domain.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 응답")
public record UserResponseDto(
	@Schema(description = "사용자 ID", example = "1")
	Long id,

	@Schema(description = "사용자 이름", example = "홍길동")
	String name,

	@Schema(description = "나이 (한국 나이)", example = "25")
	Integer age,

	@Schema(description = "생성일시", example = "2024-01-15T10:30:00", type = "string")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime createdAt,

	@Schema(description = "수정일시", example = "2024-01-15T10:30:00", type = "string")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime updatedAt
) {
	public static UserResponseDto from(User user) {
		return new UserResponseDto(
			user.getId(),
			user.getName(),
			user.getAge(),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
