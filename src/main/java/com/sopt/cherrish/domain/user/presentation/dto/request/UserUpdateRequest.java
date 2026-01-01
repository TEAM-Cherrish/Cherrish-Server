package com.sopt.cherrish.domain.user.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "사용자 정보 수정 요청")
public class UserUpdateRequest {

	@Schema(description = "수정할 이름 (선택)", example = "김철수")
	private String name;

	@Schema(description = "수정할 나이 (선택)", example = "30")
	@Min(value = 1, message = "나이는 1세 이상이어야 합니다")
	@Max(value = 150, message = "나이는 150세 이하여야 합니다")
	private Integer age;

	// 부분 업데이트를 위해 모든 필드를 nullable로 설정
}
