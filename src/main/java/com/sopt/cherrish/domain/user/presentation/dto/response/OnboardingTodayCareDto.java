package com.sopt.cherrish.domain.user.presentation.dto.response;

import java.util.Collections;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "오늘의 케어 정보")
public class OnboardingTodayCareDto {

	@Schema(description = "루틴 목록 (온보딩 시 빈 배열)", example = "[]")
	private List<Object> routines;

	public static OnboardingTodayCareDto empty() {
		return OnboardingTodayCareDto.builder()
			.routines(Collections.emptyList())
			.build();
	}
}
