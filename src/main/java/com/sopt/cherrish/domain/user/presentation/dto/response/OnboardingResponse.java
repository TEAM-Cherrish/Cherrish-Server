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
public class OnboardingResponse {

	@Schema(description = "사용자 이름", example = "홍길동")
	private String name;

	@Schema(description = "가입일시", example = "2024-01-15T10:30:00", type = "string")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime date;

	@Schema(description = "주간 연속 기록 (온보딩 시 null)", example = "null", nullable = true)
	private Integer weeklyStreak;

	@Schema(description = "오늘 상태 (온보딩 시 null)", example = "null", nullable = true)
	private String todayStatus;

	@Schema(description = "오늘의 케어")
	private OnboardingTodayCareDto todayCare;

	// Entity -> DTO 변환
	public static OnboardingResponse from(User user) {
		return OnboardingResponse.builder()
			.name(user.getName())
			.date(user.getCreatedAt())
			.weeklyStreak(null)  // 온보딩 시에는 null
			.todayStatus(null)   // 온보딩 시에는 null
			.todayCare(OnboardingTodayCareDto.empty())  // 빈 routines 배열
			.build();
	}
}
