package com.sopt.cherrish.domain.procedure.presentation.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "시술 목록 응답")
public class ProcedureListResponseDto {

	@Schema(description = "시술 목록")
	private List<ProcedureResponseDto> procedures;

	public static ProcedureListResponseDto of(List<ProcedureResponseDto> procedures) {
		return ProcedureListResponseDto.builder()
			.procedures(procedures)
			.build();
	}
}
