package com.sopt.cherrish.domain.procedure.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시술 검색 요청")
public record ProcedureSearchRequestDto(
	@Schema(description = "검색 키워드 (시술명 검색)", example = "레이저")
	String keyword,

	@Schema(description = "피부 고민 ID", example = "1")
	Long worryId
) {
}
