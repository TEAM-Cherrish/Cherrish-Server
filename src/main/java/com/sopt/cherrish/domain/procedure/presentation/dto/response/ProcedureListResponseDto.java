package com.sopt.cherrish.domain.procedure.presentation.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시술 목록 응답")
public record ProcedureListResponseDto(
	@Schema(description = "시술 목록")
	List<ProcedureResponseDto> procedures
) {
	public static ProcedureListResponseDto of(List<ProcedureResponseDto> procedures) {
		return new ProcedureListResponseDto(procedures);
	}
}
