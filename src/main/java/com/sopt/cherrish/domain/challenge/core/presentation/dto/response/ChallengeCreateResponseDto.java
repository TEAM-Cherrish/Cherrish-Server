package com.sopt.cherrish.domain.challenge.core.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeRoutine;

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

	@Schema(description = "생성된 루틴 리스트")
	List<ChallengeRoutineResponseDto> routines
) {
	public static ChallengeCreateResponseDto from(
		Challenge challenge, List<ChallengeRoutine> routines, int totalRoutineCount) {
		List<ChallengeRoutineResponseDto> routineDtos = routines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		return new ChallengeCreateResponseDto(
			challenge.getId(),
			challenge.getTitle(),
			challenge.getTotalDays(),
			challenge.getStartDate(),
			challenge.getEndDate(),
			totalRoutineCount,
			routineDtos
		);
	}

	public static ChallengeCreateResponseDto from(
		DemoChallenge challenge, List<DemoChallengeRoutine> routines, int totalRoutineCount) {
		List<ChallengeRoutineResponseDto> routineDtos = routines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		return new ChallengeCreateResponseDto(
			challenge.getId(),
			challenge.getTitle(),
			challenge.getTotalDays(),
			challenge.getStartDate(),
			challenge.getEndDate(),
			totalRoutineCount,
			routineDtos
		);
	}
}
