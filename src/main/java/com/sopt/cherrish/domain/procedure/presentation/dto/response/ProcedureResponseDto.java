package com.sopt.cherrish.domain.procedure.presentation.dto.response;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "시술 정보 응답")
public class ProcedureResponseDto {

	@Schema(description = "시술 ID", example = "1")
	private Long id;

	@Schema(description = "시술명", example = "레이저 토닝")
	private String name;

	@Schema(description = "카테고리", example = "레이저")
	private String category;

	@Schema(description = "최소 다운타임 일수", example = "1")
	private int minDowntimeDays;

	@Schema(description = "최대 다운타임 일수", example = "5")
	private int maxDowntimeDays;

	public static ProcedureResponseDto from(Procedure procedure) {
		return ProcedureResponseDto.builder()
			.id(procedure.getId())
			.name(procedure.getName())
			.category(procedure.getCategory())
			.minDowntimeDays(procedure.getMinDowntimeDays())
			.maxDowntimeDays(procedure.getMaxDowntimeDays())
			.build();
	}
}
