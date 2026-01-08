package com.sopt.cherrish.domain.userprocedure.presentation.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "사용자 시술 일정 등록 요청")
public class UserProcedureCreateRequestDto {

	@Schema(description = "예약 날짜 및 시간", example = "2025-01-01T16:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "예약 날짜 및 시간은 필수입니다")
	private LocalDateTime scheduledAt;

	@Schema(description = "시술 목록", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotEmpty(message = "시술 목록은 비어 있을 수 없습니다")
	@Valid
	private List<UserProcedureCreateRequestItemDto> procedures;

	@JsonCreator
	public UserProcedureCreateRequestDto(
		@JsonProperty("scheduledAt") LocalDateTime scheduledAt,
		@JsonProperty("procedures") List<UserProcedureCreateRequestItemDto> procedures
	) {
		this.scheduledAt = scheduledAt;
		this.procedures = procedures;
	}

	public List<UserProcedure> toEntities(User user, List<Procedure> procedures) {
		Map<Long, Procedure> procedureMap = procedures.stream()
			.collect(Collectors.toMap(Procedure::getId, Function.identity()));

		return this.procedures.stream()
			.map(item -> UserProcedure.builder()
				.user(user)
				.procedure(procedureMap.get(item.getProcedureId()))
				.scheduledAt(scheduledAt)
				.downtimeDays(item.getDowntimeDays())
				.build())
			.toList();
	}
}
