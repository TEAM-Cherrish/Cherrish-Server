package com.sopt.cherrish.domain.calendar.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "월별 캘린더 조회 요청")
public record CalendarMonthlyRequestDto(
	@Schema(description = "연도", example = "2026", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "연도는 필수입니다")
	@Min(value = 2000, message = "연도는 2000년 이상이어야 합니다")
	@Max(value = 2100, message = "연도는 2100년 이하여야 합니다")
	Integer year,

	@Schema(description = "월", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "월은 필수입니다")
	@Min(value = 1, message = "월은 1 이상이어야 합니다")
	@Max(value = 12, message = "월은 12 이하여야 합니다")
	Integer month
) {
}
