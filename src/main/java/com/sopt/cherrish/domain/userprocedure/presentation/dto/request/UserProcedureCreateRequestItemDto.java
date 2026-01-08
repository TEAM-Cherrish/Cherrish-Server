package com.sopt.cherrish.domain.userprocedure.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "사용자 시술 항목 요청")
public class UserProcedureCreateRequestItemDto {

	@Schema(description = "시술 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "시술 ID는 필수입니다")
	private Long procedureId;

	@Schema(description = "개인 다운타임(일)", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "개인 다운타임은 필수입니다")
	@Min(value = 0, message = "다운타임은 0 이상이어야 합니다")
	private Integer downtimeDays;

	@JsonCreator
	public UserProcedureCreateRequestItemDto(
		@JsonProperty("procedureId") Long procedureId,
		@JsonProperty("downtimeDays") Integer downtimeDays
	) {
		this.procedureId = procedureId;
		this.downtimeDays = downtimeDays;
	}
}
