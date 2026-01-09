package com.sopt.cherrish.domain.challenge.core.presentation.dto.response;

import java.util.List;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 상세 조회 응답")
public record ChallengeDetailResponseDto(
	@Schema(description = "챌린지 ID", example = "1")
	Long challengeId,

	@Schema(description = "챌린지 제목", example = "7일 보습 챌린지")
	String title,

	@Schema(description = "현재 일차", example = "3")
	int currentDay,

	@Schema(description = "전체 진행률 (%)", example = "37.5")
	double progressPercentage,

	@Schema(description = "체리 레벨 (1-4)", example = "2")
	int cherryLevel,

	@Schema(description = "현재 레벨 내 진척도 (%)", example = "50.0")
	double progressToNextLevel,

	@Schema(description = "오늘의 루틴 리스트")
	List<ChallengeRoutineResponseDto> todayRoutines,

	@Schema(description = "응원 메시지", example = "3일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!")
	String cheeringMessage
) {
	public static ChallengeDetailResponseDto from(
		Challenge challenge,
		int currentDay,
		ChallengeStatistics statistics,
		List<ChallengeRoutine> todayRoutines,
		String cheeringMessage
	) {
		List<ChallengeRoutineResponseDto> routineDtos = todayRoutines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		return new ChallengeDetailResponseDto(
			challenge.getId(),
			challenge.getTitle(),
			currentDay,
			statistics.getProgressPercentage(),
			statistics.getCherryLevel(),
			statistics.getProgressToNextLevel(),
			routineDtos,
			cheeringMessage
		);
	}
}
