package com.sopt.cherrish.domain.challenge.core.presentation.dto.response;

import java.util.List;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커스텀 루틴 추가 응답")
public record CustomRoutineAddResponseDto(
	@Schema(description = "챌린지 ID", example = "1")
	Long challengeId,

	@Schema(description = "추가된 루틴 이름", example = "저녁 마사지")
	String routineName,

	@Schema(description = "추가된 루틴 개수", example = "5")
	int addedCount,

	@Schema(description = "추가된 루틴 리스트")
	List<ChallengeRoutineResponseDto> routines,

	@Schema(description = "업데이트된 총 루틴 개수", example = "26")
	int totalRoutineCount,

	@Schema(description = "상태 메시지", example = "오늘부터 5일간 '저녁 마사지' 루틴이 추가되었습니다.")
	String message
) {
	public static CustomRoutineAddResponseDto from(
		Challenge challenge,
		String routineName,
		List<ChallengeRoutine> addedRoutines,
		int totalRoutineCount
	) {
		List<ChallengeRoutineResponseDto> routineDtos = addedRoutines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		int addedCount = addedRoutines.size();
		String message = String.format(
			"오늘부터 %d일간 '%s' 루틴이 추가되었습니다.",
			addedCount,
			routineName
		);

		return new CustomRoutineAddResponseDto(
			challenge.getId(),
			routineName,
			addedCount,
			routineDtos,
			totalRoutineCount,
			message
		);
	}
}
