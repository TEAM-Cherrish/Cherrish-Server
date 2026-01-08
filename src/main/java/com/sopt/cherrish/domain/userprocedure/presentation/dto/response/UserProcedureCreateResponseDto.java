package com.sopt.cherrish.domain.userprocedure.presentation.dto.response;

import java.util.List;

import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 시술 일정 등록 응답")
public class UserProcedureCreateResponseDto {

	@Schema(description = "등록된 시술 일정 목록")
	private List<UserProcedureResponseDto> procedures;

	public static UserProcedureCreateResponseDto from(List<UserProcedure> userProcedures) {
		List<UserProcedureResponseDto> responses = userProcedures.stream()
			.map(UserProcedureResponseDto::from)
			.toList();
		return UserProcedureCreateResponseDto.builder()
			.procedures(responses)
			.build();
	}
}
