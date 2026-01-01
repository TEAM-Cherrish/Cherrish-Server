package com.sopt.cherrish.domain.user.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "사용자 정보 수정 요청")
public class UserUpdateRequestDto {

	@Schema(description = "수정할 이름 (선택)", example = "김철수")
	@Size(max = 50, message = "이름은 50자를 초과할 수 없습니다")
	private String name;

	@Schema(description = "수정할 나이 (선택)", example = "30")
	@Min(value = 1, message = "나이는 1세 이상이어야 합니다")
	@Max(value = 150, message = "나이는 150세 이하여야 합니다")
	private Integer age;

}
