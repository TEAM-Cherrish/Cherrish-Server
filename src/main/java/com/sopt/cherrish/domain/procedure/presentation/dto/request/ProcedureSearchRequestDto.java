package com.sopt.cherrish.domain.procedure.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "시술 검색 요청")
public class ProcedureSearchRequestDto {

	@Schema(description = "검색 키워드 (시술명 검색)", example = "레이저")
	private String keyword;

	@Schema(description = "피부 고민 ID", example = "1")
	private Long worryId;
}