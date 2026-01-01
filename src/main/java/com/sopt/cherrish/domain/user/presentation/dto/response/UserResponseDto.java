package com.sopt.cherrish.domain.user.presentation.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.user.domain.model.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 정보 응답")
public class UserResponseDto {

	@Schema(description = "사용자 ID", example = "1")
	private Long id;

	@Schema(description = "사용자 이름", example = "홍길동")
	private String name;

	@Schema(description = "나이 (한국 나이)", example = "25")
	private Integer age;

	@Schema(description = "생성일시", example = "2024-01-15T10:30:00")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@Schema(description = "수정일시", example = "2024-01-15T10:30:00")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime updatedAt;

	// Entity -> DTO 변환
	public static UserResponseDto from(User user) {
		return UserResponseDto.builder()
			.id(user.getId())
			.name(user.getName())
			.age(user.getAge())
			.createdAt(user.getCreatedAt())
			.updatedAt(user.getUpdatedAt())
			.build();
	}
}
