package com.sopt.cherrish.domain.challenge.core.presentation;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_CHALLENGE_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_CHALLENGE_TITLE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_ROUTINE_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_USER_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createMockChallengeCreateResponse;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createMockChallengeDetailResponse;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createMockRoutineCompletionResponse;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRequestWithEmptyRoutines;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRequestWithEmptyTitle;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRequestWithNullTitle;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRequestWithTooManyRoutines;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createValidChallengeRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeCreationFacade;
import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeQueryFacade;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;

@WebMvcTest(ChallengeController.class)
@DisplayName("ChallengeController 통합 테스트")
class ChallengeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ChallengeCreationFacade challengeCreationFacade;

	@MockitoBean
	private ChallengeQueryFacade challengeQueryFacade;

	@MockitoBean
	private ChallengeRoutineService challengeRoutineService;

	@Nested
	@DisplayName("POST /api/challenges/{userId} - 챌린지 생성")
	class CreateChallenge {

		@Test
		@DisplayName("성공 - 챌린지 생성 및 루틴 21개 생성 (3개 루틴명 x 7일)")
		void success() throws Exception {
			// given
			ChallengeCreateRequestDto request = createValidChallengeRequest();
			ChallengeCreateResponseDto response = createMockChallengeCreateResponse();

			given(challengeCreationFacade.createChallenge(eq(DEFAULT_USER_ID), any(ChallengeCreateRequestDto.class)))
				.willReturn(response);

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.challengeId").value(DEFAULT_CHALLENGE_ID))
				.andExpect(jsonPath("$.data.title").value(DEFAULT_CHALLENGE_TITLE))
				.andExpect(jsonPath("$.data.totalRoutineCount").value(21));
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 사용자")
		void failUserNotFound() throws Exception {
			// given
			Long invalidUserId = 999L;
			ChallengeCreateRequestDto request = createValidChallengeRequest();

			given(challengeCreationFacade.createChallenge(eq(invalidUserId), any(ChallengeCreateRequestDto.class)))
				.willThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", invalidUserId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 이미 활성 챌린지가 존재함")
		void failDuplicateActiveChallenge() throws Exception {
			// given
			ChallengeCreateRequestDto request = createValidChallengeRequest();

			given(challengeCreationFacade.createChallenge(eq(DEFAULT_USER_ID), any(ChallengeCreateRequestDto.class)))
				.willThrow(new ChallengeException(ChallengeErrorCode.DUPLICATE_ACTIVE_CHALLENGE));

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (빈 제목)")
		void failValidation() throws Exception {
			// given
			ChallengeCreateRequestDto request = createRequestWithEmptyTitle();

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("GET /api/challenges/{userId} - 활성 챌린지 조회")
	class GetActiveChallenge {

		@Test
		@DisplayName("성공 - 활성 챌린지 상세 조회")
		void success() throws Exception {
			// given
			ChallengeDetailResponseDto response = createMockChallengeDetailResponse();

			given(challengeQueryFacade.getActiveChallengeDetail(DEFAULT_USER_ID))
				.willReturn(response);

			// when & then
			mockMvc.perform(get("/api/challenges/{userId}", DEFAULT_USER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.challengeId").value(DEFAULT_CHALLENGE_ID))
				.andExpect(jsonPath("$.data.currentDay").value(3))
				.andExpect(jsonPath("$.data.todayRoutines").isArray());
		}

		@Test
		@DisplayName("실패 - 활성 챌린지가 없을 때")
		void failNoChallengeFound() throws Exception {
			// given
			given(challengeQueryFacade.getActiveChallengeDetail(DEFAULT_USER_ID))
				.willThrow(new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

			// when & then
			mockMvc.perform(get("/api/challenges/{userId}", DEFAULT_USER_ID))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 사용자")
		void failUserNotFound() throws Exception {
			// given
			Long invalidUserId = 999L;

			given(challengeQueryFacade.getActiveChallengeDetail(invalidUserId))
				.willThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

			// when & then
			mockMvc.perform(get("/api/challenges/{userId}", invalidUserId))
				.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("PATCH /api/challenges/{userId}/routines/{routineId} - 루틴 완료 토글")
	class ToggleRoutineCompletion {

		@ParameterizedTest
		@CsvSource({
			"true, 루틴을 완료했습니다!",
			"false, 루틴 완료를 취소했습니다."
		})
		@DisplayName("성공 - 루틴 완료 상태 토글")
		void successToggle(boolean isComplete, String message) throws Exception {
			// given
			RoutineCompletionResponseDto response = createMockRoutineCompletionResponse(isComplete);

			given(challengeRoutineService.toggleCompletion(DEFAULT_USER_ID, DEFAULT_ROUTINE_ID))
				.willReturn(response);

			// when & then
			mockMvc.perform(patch("/api/challenges/{userId}/routines/{routineId}", DEFAULT_USER_ID, DEFAULT_ROUTINE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.routineId").value(DEFAULT_ROUTINE_ID))
				.andExpect(jsonPath("$.data.isComplete").value(isComplete));
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 루틴")
		void failRoutineNotFound() throws Exception {
			// given
			Long invalidRoutineId = 999L;

			given(challengeRoutineService.toggleCompletion(DEFAULT_USER_ID, invalidRoutineId))
				.willThrow(new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND));

			// when & then
			mockMvc.perform(patch("/api/challenges/{userId}/routines/{routineId}", DEFAULT_USER_ID, invalidRoutineId))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 다른 사용자의 루틴에 접근")
		void failUnauthorizedAccess() throws Exception {
			// given
			given(challengeRoutineService.toggleCompletion(DEFAULT_USER_ID, DEFAULT_ROUTINE_ID))
				.willThrow(new ChallengeException(ChallengeErrorCode.UNAUTHORIZED_ACCESS));

			// when & then
			mockMvc.perform(patch("/api/challenges/{userId}/routines/{routineId}", DEFAULT_USER_ID, DEFAULT_ROUTINE_ID))
				.andExpect(status().isForbidden());
		}
	}

	@Nested
	@DisplayName("유효성 검증 테스트")
	class ValidationTests {

		@Test
		@DisplayName("실패 - null 제목")
		void failNullTitle() throws Exception {
			// given
			ChallengeCreateRequestDto request = createRequestWithNullTitle();

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 빈 루틴 목록")
		void failEmptyRoutines() throws Exception {
			// given
			ChallengeCreateRequestDto request = createRequestWithEmptyRoutines();

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 루틴 개수 초과")
		void failTooManyRoutines() throws Exception {
			// given
			ChallengeCreateRequestDto request = createRequestWithTooManyRoutines();

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}
	}
}
