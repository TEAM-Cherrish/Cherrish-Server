package com.sopt.cherrish.domain.user.presentation.dto.request;

import com.sopt.cherrish.domain.user.domain.model.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "온보딩 프로필 생성 요청")
public class OnboardingRequest {

	@Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "이름은 필수입니다")
	private String name;

	@Schema(description = "나이 (한국 나이)", example = "25", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "나이는 필수입니다")
	@Min(value = 1, message = "나이는 1세 이상이어야 합니다")
	@Max(value = 150, message = "나이는 150세 이하여야 합니다")
	private Integer age;

	// DTO -> Entity 변환
	public User toEntity() {
		return User.builder()
			.name(this.name)
			.age(this.age)
			.build();
	}
}
