package com.sopt.cherrish.domain.userprocedure.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 시술 항목 요청")
public record UserProcedureCreateRequestItemDto(
	@Schema(description = "시술 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "시술 ID는 필수입니다")
	Long procedureId,

	@Schema(description = "개인 다운타임(일)", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "개인 다운타임은 필수입니다")
	@Min(value = 0, message = "다운타임은 0일 이상이어야 합니다")
	@Max(value = 30, message = "다운타임은 30일 이하여야 합니다")
	Integer downtimeDays
) {
}
