package com.sopt.cherrish.domain.userprocedure.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestItemDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureCreateResponseDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureResponseDto;

public class UserProcedureTestFixture {

	public static final LocalDateTime DEFAULT_SCHEDULED_AT = LocalDateTime.of(2025, 1, 1, 16, 0);
	public static final String DEFAULT_SCHEDULED_AT_STRING = "2025-01-01T16:00:00";
	public static final LocalDate DEFAULT_RECOVERY_TARGET_DATE = LocalDate.of(2025, 1, 10);
	public static final String DEFAULT_RECOVERY_TARGET_DATE_STRING = "2025-01-10";

	private UserProcedureTestFixture() {
		// Utility class
	}

	public static UserProcedureCreateRequestDto createValidRequest() {
		return new UserProcedureCreateRequestDto(
			DEFAULT_SCHEDULED_AT,
			DEFAULT_RECOVERY_TARGET_DATE,
			List.of(
				new UserProcedureCreateRequestItemDto(1L, 6),
				new UserProcedureCreateRequestItemDto(2L, 3)
			)
		);
	}

	public static UserProcedureCreateRequestDto createRequestWithSingleProcedure(Long procedureId, Integer downtimeDays) {
		return new UserProcedureCreateRequestDto(
			DEFAULT_SCHEDULED_AT,
			null,
			List.of(new UserProcedureCreateRequestItemDto(procedureId, downtimeDays))
		);
	}

	public static UserProcedureCreateResponseDto createValidResponse() {
		return new UserProcedureCreateResponseDto(List.of(
			new UserProcedureResponseDto(
				10L,
				1L,
				"레이저 토닝",
				DEFAULT_SCHEDULED_AT,
				6,
				DEFAULT_RECOVERY_TARGET_DATE
			),
			new UserProcedureResponseDto(
				11L,
				2L,
				"필러",
				DEFAULT_SCHEDULED_AT,
				3,
				DEFAULT_RECOVERY_TARGET_DATE
			)
		));
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
