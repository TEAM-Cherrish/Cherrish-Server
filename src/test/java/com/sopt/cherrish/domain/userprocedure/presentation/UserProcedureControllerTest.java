package com.sopt.cherrish.domain.userprocedure.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.procedure.exception.ProcedureException;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.userprocedure.application.service.UserProcedureService;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestItemDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureCreateResponseDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureResponseDto;

@WebMvcTest(UserProcedureController.class)
@DisplayName("UserProcedureController 통합 테스트")
class UserProcedureControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserProcedureService userProcedureService;

	@Nested
	@DisplayName("POST /api/users/{userId}/procedures - 사용자 시술 일정 등록")
	class CreateUserProcedures {

		@Test
		@DisplayName("성공 - 시술 일정 등록")
		void success() throws Exception {
			// given
			Long userId = 1L;
			LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 1, 16, 0);
			UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
				scheduledAt,
				List.of(
					new UserProcedureCreateRequestItemDto(1L, 6),
					new UserProcedureCreateRequestItemDto(2L, 3)
				)
			);

			UserProcedureCreateResponseDto response = UserProcedureCreateResponseDto.builder()
				.procedures(List.of(
					UserProcedureResponseDto.builder()
						.userProcedureId(10L)
						.procedureId(1L)
						.procedureName("레이저 토닝")
						.scheduledAt(scheduledAt)
						.downtimeDays(6)
						.build(),
					UserProcedureResponseDto.builder()
						.userProcedureId(11L)
						.procedureId(2L)
						.procedureName("필러")
						.scheduledAt(scheduledAt)
						.downtimeDays(3)
						.build()
				))
				.build();

			given(userProcedureService.createUserProcedures(eq(userId), any(UserProcedureCreateRequestDto.class)))
				.willReturn(response);

			// when & then
			mockMvc.perform(post("/api/users/{userId}/procedures", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("S200"))
				.andExpect(jsonPath("$.message").value("성공"))
				.andExpect(jsonPath("$.data.procedures").isArray())
				.andExpect(jsonPath("$.data.procedures.length()").value(2))
				.andExpect(jsonPath("$.data.procedures[0].userProcedureId").value(10))
				.andExpect(jsonPath("$.data.procedures[0].procedureId").value(1))
				.andExpect(jsonPath("$.data.procedures[0].procedureName").value("레이저 토닝"))
				.andExpect(jsonPath("$.data.procedures[0].scheduledAt").value("2025-01-01T16:00:00"))
				.andExpect(jsonPath("$.data.procedures[0].downtimeDays").value(6));
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 사용자")
		void failUserNotFound() throws Exception {
			// given
			Long userId = 999L;
			UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
				LocalDateTime.of(2025, 1, 1, 16, 0),
				List.of(new UserProcedureCreateRequestItemDto(1L, 6))
			);

			given(userProcedureService.createUserProcedures(eq(userId), any(UserProcedureCreateRequestDto.class)))
				.willThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/users/{userId}/procedures", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 시술")
		void failProcedureNotFound() throws Exception {
			// given
			Long userId = 1L;
			UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
				LocalDateTime.of(2025, 1, 1, 16, 0),
				List.of(new UserProcedureCreateRequestItemDto(999L, 6))
			);

			given(userProcedureService.createUserProcedures(eq(userId), any(UserProcedureCreateRequestDto.class)))
				.willThrow(new ProcedureException(ProcedureErrorCode.PROCEDURE_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/users/{userId}/procedures", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (빈 시술 목록)")
		void failValidationEmptyProcedures() throws Exception {
			// given
			String invalidRequest = """
				{
					"scheduledAt": "2025-01-01T16:00:00",
					"procedures": []
				}
				""";

			// when & then
			mockMvc.perform(post("/api/users/{userId}/procedures", 1L)
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidRequest))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (다운타임 음수)")
		void failValidationNegativeDowntime() throws Exception {
			// given
			String invalidRequest = """
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

			// when & then
			mockMvc.perform(post("/api/users/{userId}/procedures", 1L)
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidRequest))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (예약 날짜 누락)")
		void failValidationMissingScheduledAt() throws Exception {
			// given
			String invalidRequest = """
				{
					"procedures": [
						{
							"procedureId": 1,
							"downtimeDays": 3
						}
					]
				}
				""";

			// when & then
			mockMvc.perform(post("/api/users/{userId}/procedures", 1L)
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidRequest))
				.andExpect(status().isBadRequest());
		}
	}
}
