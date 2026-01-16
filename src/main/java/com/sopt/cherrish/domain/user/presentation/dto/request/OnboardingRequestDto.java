package com.sopt.cherrish.domain.user.presentation.dto.request;

import com.sopt.cherrish.domain.user.domain.model.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "온보딩 프로필 생성 요청")
public record OnboardingRequestDto(
	@Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "이름은 필수입니다")
	@Size(max = 7, message = "이름은 7자를 초과할 수 없습니다")
	String name,

	@Schema(description = "나이 (한국 나이)", example = "25", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "나이는 필수입니다")
	@Min(value = 1, message = "나이는 1세 이상이어야 합니다")
	@Max(value = 100, message = "나이는 100세 이하여야 합니다")
	Integer age
) {
	// DTO -> Entity 변환
	public User toEntity() {
		return User.builder()
			.name(this.name)
			.age(this.age)
			.build();
	}
}
