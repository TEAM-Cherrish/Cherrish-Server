package com.sopt.cherrish.domain.challenge.core.presentation.dto.response;

import java.util.List;

import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeRoutine;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "루틴 일괄 업데이트 응답")
public record RoutineBatchUpdateResponseDto(
	@Schema(description = "업데이트된 루틴 목록")
	List<ChallengeRoutineResponseDto> routines,

	@Schema(description = "업데이트된 루틴 개수", example = "3")
	int updatedCount
) {
	public static RoutineBatchUpdateResponseDto from(List<ChallengeRoutine> routines) {
		List<ChallengeRoutineResponseDto> routineDtos = routines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		int count = routines.size();

		return new RoutineBatchUpdateResponseDto(routineDtos, count);
	}

	public static RoutineBatchUpdateResponseDto fromDemoRoutines(List<DemoChallengeRoutine> routines) {
		List<ChallengeRoutineResponseDto> routineDtos = routines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		int count = routines.size();

		return new RoutineBatchUpdateResponseDto(routineDtos, count);
	}
}
