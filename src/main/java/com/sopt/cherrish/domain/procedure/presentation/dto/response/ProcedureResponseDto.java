package com.sopt.cherrish.domain.procedure.presentation.dto.response;

import java.util.List;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시술 정보 응답")
public record ProcedureResponseDto(
	@Schema(description = "시술 ID", example = "1")
	Long id,

	@Schema(description = "시술명", example = "레이저 토닝")
	String name,

	@Schema(description = "피부 고민 목록")
	List<ProcedureWorryResponseDto> worries,

	@Schema(description = "최소 다운타임 일수", example = "1")
	int minDowntimeDays,

	@Schema(description = "최대 다운타임 일수", example = "5")
	int maxDowntimeDays
) {
	public static ProcedureResponseDto from(Procedure procedure, List<ProcedureWorryResponseDto> worries) {
		return new ProcedureResponseDto(
			procedure.getId(),
			procedure.getName(),
			worries,
			procedure.getMinDowntimeDays(),
			procedure.getMaxDowntimeDays()
		);
	}
}
