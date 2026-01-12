package com.sopt.cherrish.domain.challenge.core.presentation.dto.response;

import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeRoutine;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "루틴 완료 토글 응답")
public record RoutineCompletionResponseDto(
	@Schema(description = "루틴 ID", example = "1")
	Long routineId,

	@Schema(description = "루틴명", example = "아침 세안")
	String name,

	@Schema(description = "완료 여부", example = "true")
	boolean isComplete,

	@Schema(description = "상태 메시지", example = "루틴을 완료했습니다!")
	String message
) {
	public static RoutineCompletionResponseDto from(ChallengeRoutine routine) {
		String message = routine.getIsComplete()
			? "루틴을 완료했습니다!"
			: "루틴 완료를 취소했습니다.";

		return new RoutineCompletionResponseDto(
			routine.getId(),
			routine.getName(),
			routine.getIsComplete(),
			message
		);
	}

	public static RoutineCompletionResponseDto from(DemoChallengeRoutine routine) {
		String message = routine.getIsComplete()
			? "루틴을 완료했습니다!"
			: "루틴 완료를 취소했습니다.";

		return new RoutineCompletionResponseDto(
			routine.getId(),
			routine.getName(),
			routine.getIsComplete(),
			message
		);
	}
}
