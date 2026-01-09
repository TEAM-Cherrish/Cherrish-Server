package com.sopt.cherrish.domain.challenge.core.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "루틴 업데이트 항목")
public class RoutineUpdateItemDto {

	@Schema(description = "루틴 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "루틴 ID는 필수입니다")
	private Long routineId;

	@Schema(description = "완료 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "완료 여부는 필수입니다")
	private Boolean isComplete;

	@JsonCreator
	public RoutineUpdateItemDto(
		@JsonProperty("routineId") Long routineId,
		@JsonProperty("isComplete") Boolean isComplete
	) {
		this.routineId = routineId;
		this.isComplete = isComplete;
	}
}
