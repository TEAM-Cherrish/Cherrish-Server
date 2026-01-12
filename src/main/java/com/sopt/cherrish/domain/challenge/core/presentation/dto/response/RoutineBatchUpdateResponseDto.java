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
	int updatedCount,

	@Schema(description = "상태 메시지", example = "3개의 루틴이 업데이트되었습니다.")
	String message
) {
	public static RoutineBatchUpdateResponseDto from(List<ChallengeRoutine> routines) {
		List<ChallengeRoutineResponseDto> routineDtos = routines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		int count = routines.size();
		String message = count + "개의 루틴이 업데이트되었습니다.";

		return new RoutineBatchUpdateResponseDto(routineDtos, count, message);
	}

	public static RoutineBatchUpdateResponseDto fromDemoRoutines(List<DemoChallengeRoutine> routines) {
		List<ChallengeRoutineResponseDto> routineDtos = routines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		int count = routines.size();
		String message = count + "개의 루틴이 업데이트되었습니다.";

		return new RoutineBatchUpdateResponseDto(routineDtos, count, message);
	}
}
