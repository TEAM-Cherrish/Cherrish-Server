package com.sopt.cherrish.domain.procedure.presentation.dto.response;

import com.sopt.cherrish.domain.worry.domain.model.Worry;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "시술 관련 피부 고민 응답")
public class ProcedureWorryResponseDto {

	@Schema(description = "피부 고민 ID", example = "1")
	private Long id;

	@Schema(description = "피부 고민 내용", example = "여드름/트러블")
	private String content;

	public static ProcedureWorryResponseDto from(Worry worry) {
		return ProcedureWorryResponseDto.builder()
			.id(worry.getId())
			.content(worry.getContent())
			.build();
	}
}
