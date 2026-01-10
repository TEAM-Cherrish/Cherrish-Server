package com.sopt.cherrish.domain.calendar.presentation.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "일자별 시술 상세 조회 요청")
public record CalendarDailyRequestDto(

	@Schema(description = "날짜", example = "2026-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "날짜는 필수입니다")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	LocalDate date
) {
}
