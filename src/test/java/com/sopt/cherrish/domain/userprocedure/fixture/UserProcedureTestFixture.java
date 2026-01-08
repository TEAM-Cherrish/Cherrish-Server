package com.sopt.cherrish.domain.userprocedure.fixture;

import java.time.LocalDateTime;
import java.util.List;

import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestItemDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureCreateResponseDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureResponseDto;

public class UserProcedureTestFixture {

	public static final LocalDateTime DEFAULT_SCHEDULED_AT = LocalDateTime.of(2025, 1, 1, 16, 0);
	public static final String DEFAULT_SCHEDULED_AT_STRING = "2025-01-01T16:00:00";

	private UserProcedureTestFixture() {
		// Utility class
	}

	public static UserProcedureCreateRequestDto createValidRequest() {
		return new UserProcedureCreateRequestDto(
			DEFAULT_SCHEDULED_AT,
			List.of(
				new UserProcedureCreateRequestItemDto(1L, 6),
				new UserProcedureCreateRequestItemDto(2L, 3)
			)
		);
	}

	public static UserProcedureCreateRequestDto createRequestWithSingleProcedure(Long procedureId, Integer downtimeDays) {
		return new UserProcedureCreateRequestDto(
			DEFAULT_SCHEDULED_AT,
			List.of(new UserProcedureCreateRequestItemDto(procedureId, downtimeDays))
		);
	}

	public static UserProcedureCreateResponseDto createValidResponse() {
		return UserProcedureCreateResponseDto.builder()
			.procedures(List.of(
				UserProcedureResponseDto.builder()
					.userProcedureId(10L)
					.procedureId(1L)
					.procedureName("레이저 토닝")
					.scheduledAt(DEFAULT_SCHEDULED_AT)
					.downtimeDays(6)
					.build(),
				UserProcedureResponseDto.builder()
					.userProcedureId(11L)
					.procedureId(2L)
					.procedureName("필러")
					.scheduledAt(DEFAULT_SCHEDULED_AT)
					.downtimeDays(3)
					.build()
			))
			.build();
	}

	public static String createInvalidRequestWithEmptyProcedures() {
		return """
			{
				"scheduledAt": "2025-01-01T16:00:00",
				"procedures": []
			}
			""";
	}

	public static String createInvalidRequestWithNegativeDowntime() {
		return """
			{
				"scheduledAt": "2025-01-01T16:00:00",
				"procedures": [
					{
						"procedureId": 1,
						"downtimeDays": -1
					}
				]
			}
			""";
	}

	public static String createInvalidRequestMissingScheduledAt() {
		return """
			{
				"procedures": [
					{
						"procedureId": 1,
						"downtimeDays": 3
					}
				]
			}
			""";
	}
}
