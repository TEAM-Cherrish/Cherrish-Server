package com.sopt.cherrish.domain.challenge.core.presentation.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "루틴 일괄 업데이트 요청")
public class RoutineUpdateRequestDto {

	@Schema(
		description = "업데이트할 루틴 목록",
		requiredMode = Schema.RequiredMode.REQUIRED,
		example = "[{\"routineId\": 1, \"isComplete\": true}, {\"routineId\": 2, \"isComplete\": false}]"
	)
	@NotEmpty(message = "업데이트할 루틴 목록은 비어 있을 수 없습니다")
	@Valid
	private List<RoutineUpdateItemDto> routines;

	@JsonCreator
	public RoutineUpdateRequestDto(
		@JsonProperty("routines") List<RoutineUpdateItemDto> routines
	) {
		this.routines = routines;
	}
}
