package com.sopt.cherrish.domain.userprocedure.presentation;

import static com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureTestFixture.DEFAULT_RECOVERY_TARGET_DATE_STRING;
import static com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureTestFixture.DEFAULT_SCHEDULED_AT_STRING;
import static com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureTestFixture.createInvalidRequestMissingScheduledAt;
import static com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureTestFixture.createInvalidRequestWithEmptyProcedures;
import static com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureTestFixture.createInvalidRequestWithNegativeDowntime;
import static com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureTestFixture.createRequestWithSingleProcedure;
import static com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureTestFixture.createValidRequest;
import static com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureTestFixture.createValidResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureCreateResponseDto;

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
	@DisplayName("POST /api/user-procedures - 사용자 시술 일정 등록")
	class CreateUserProcedures {

		@Test
		@DisplayName("성공 - 시술 일정 등록")
		void success() throws Exception {
			// given
			Long userId = 1L;
			UserProcedureCreateRequestDto request = createValidRequest();
			UserProcedureCreateResponseDto response = createValidResponse();

			given(userProcedureService.createUserProcedures(eq(userId), any(UserProcedureCreateRequestDto.class)))
				.willReturn(response);

			// when & then
			mockMvc.perform(post("/api/user-procedures")
				.header("X-User-Id", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("S200"))
				.andExpect(jsonPath("$.message").value("성공"))
				.andExpect(jsonPath("$.data.procedures").isArray())
				.andExpect(jsonPath("$.data.procedures.length()").value(2))
				// 첫 번째 시술 검증
				.andExpect(jsonPath("$.data.procedures[0].userProcedureId").value(10))
				.andExpect(jsonPath("$.data.procedures[0].procedureId").value(1))
				.andExpect(jsonPath("$.data.procedures[0].procedureName").value("레이저 토닝"))
				.andExpect(jsonPath("$.data.procedures[0].scheduledAt").value(DEFAULT_SCHEDULED_AT_STRING))
				.andExpect(jsonPath("$.data.procedures[0].downtimeDays").value(6))
				.andExpect(jsonPath("$.data.procedures[0].recoveryTargetDate").value(DEFAULT_RECOVERY_TARGET_DATE_STRING))
				// 두 번째 시술 검증
				.andExpect(jsonPath("$.data.procedures[1].userProcedureId").value(11))
				.andExpect(jsonPath("$.data.procedures[1].procedureId").value(2))
				.andExpect(jsonPath("$.data.procedures[1].procedureName").value("필러"))
				.andExpect(jsonPath("$.data.procedures[1].scheduledAt").value(DEFAULT_SCHEDULED_AT_STRING))
				.andExpect(jsonPath("$.data.procedures[1].downtimeDays").value(3))
				.andExpect(jsonPath("$.data.procedures[1].recoveryTargetDate").value(DEFAULT_RECOVERY_TARGET_DATE_STRING));
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 사용자")
		void failUserNotFound() throws Exception {
			// given
			Long userId = 999L;
			UserProcedureCreateRequestDto request = createRequestWithSingleProcedure(1L, 6);

			given(userProcedureService.createUserProcedures(eq(userId), any(UserProcedureCreateRequestDto.class)))
				.willThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/user-procedures")
				.header("X-User-Id", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 시술")
		void failProcedureNotFound() throws Exception {
			// given
			Long userId = 1L;
			UserProcedureCreateRequestDto request = createRequestWithSingleProcedure(999L, 6);

			given(userProcedureService.createUserProcedures(eq(userId), any(UserProcedureCreateRequestDto.class)))
				.willThrow(new ProcedureException(ProcedureErrorCode.PROCEDURE_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/user-procedures")
				.header("X-User-Id", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (빈 시술 목록)")
		void failValidationEmptyProcedures() throws Exception {
			// given
			String invalidRequest = createInvalidRequestWithEmptyProcedures();

			// when & then
			mockMvc.perform(post("/api/user-procedures")
				.header("X-User-Id", 1L)
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidRequest))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("C001"))
				.andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다"))
				.andExpect(jsonPath("$.data.procedures").value("시술 목록은 비어 있을 수 없습니다"));
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (다운타임 음수)")
		void failValidationNegativeDowntime() throws Exception {
			// given
			String invalidRequest = createInvalidRequestWithNegativeDowntime();

			// when & then
			mockMvc.perform(post("/api/user-procedures")
				.header("X-User-Id", 1L)
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidRequest))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("C001"))
				.andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다"))
				.andExpect(jsonPath("$.data['procedures[0].downtimeDays']").value("다운타임은 0일 이상이어야 합니다"));
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (예약 날짜 누락)")
		void failValidationMissingScheduledAt() throws Exception {
			// given
			String invalidRequest = createInvalidRequestMissingScheduledAt();

			// when & then
			mockMvc.perform(post("/api/user-procedures")
				.header("X-User-Id", 1L)
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalidRequest))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("C001"))
				.andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다"))
				.andExpect(jsonPath("$.data.scheduledAt").value("예약 날짜 및 시간은 필수입니다"));
		}
	}
}
