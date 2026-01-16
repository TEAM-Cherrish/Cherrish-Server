package com.sopt.cherrish.domain.worry.presentation.dto.response;

import com.sopt.cherrish.domain.worry.domain.model.Worry;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "피부 고민 응답")
public record WorryResponseDto(
	@Schema(description = "피부 고민 ID", example = "1")
	Long id,

	@Schema(description = "피부 고민 내용", example = "여드름/트러블")
	String content
) {
	public static WorryResponseDto from(Worry worry) {
		return new WorryResponseDto(
			worry.getId(),
			worry.getContent()
		);
	}
}
