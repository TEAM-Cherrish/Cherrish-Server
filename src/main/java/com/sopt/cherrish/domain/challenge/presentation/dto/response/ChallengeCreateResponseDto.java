package com.sopt.cherrish.domain.challenge.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.challenge.domain.model.Challenge;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 생성 응답")
public record ChallengeCreateResponseDto(
	@Schema(description = "챌린지 ID", example = "1")
	Long challengeId,

	@Schema(description = "챌린지 제목", example = "7일 챌린지")
	String title,

	@Schema(description = "총 일수", example = "7")
	int totalDays,

	@Schema(description = "시작일", example = "2024-01-15")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate startDate,

	@Schema(description = "종료일", example = "2024-01-21")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate endDate,

	@Schema(description = "전체 루틴 개수", example = "21")
	int totalRoutineCount,

	@Schema(description = "루틴명 리스트")
	List<String> routineNames
) {
	public static ChallengeCreateResponseDto from(
		Challenge challenge, List<String> routineNames, int totalRoutineCount) {
		return new ChallengeCreateResponseDto(
			challenge.getId(),
			challenge.getTitle(),
			challenge.getTotalDays(),
			challenge.getStartDate(),
			challenge.getEndDate(),
			totalRoutineCount,
			routineNames
		);
	}
}
