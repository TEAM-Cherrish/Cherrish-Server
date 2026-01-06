package com.sopt.cherrish.domain.procedure.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "시술 검색 요청")
public record ProcedureSearchRequestDto(
	@Schema(description = "검색 키워드 (시술명 검색)", example = "레이저")
	@Size(max = 100, message = "검색 키워드는 100자를 초과할 수 없습니다")
	@Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]*$", message = "검색 키워드는 한글, 영문, 숫자, 공백만 입력 가능합니다")
	String keyword,

	@Schema(description = "피부 고민 ID", example = "1")
	@Positive(message = "피부 고민 ID는 양수여야 합니다")
	Long worryId
) {
}
