package com.sopt.cherrish.domain.challenge.core.presentation;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_ROUTINE_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_USER_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createMockCustomRoutineAddResponse;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createMockRoutineBatchUpdateResponse;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createMockRoutineCompletionResponse;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createMockSingleRoutineBatchUpdateResponse;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRoutineUpdateRequestWithEmptyList;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRoutineUpdateRequestWithNullIsComplete;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRoutineUpdateRequestWithNullRoutineId;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createSingleRoutineUpdateRequest;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createValidRoutineUpdateRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeCustomRoutineFacade;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.CustomRoutineAddRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.CustomRoutineAddResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineBatchUpdateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;

@WebMvcTest(ChallengeRoutineController.class)
@DisplayName("ChallengeRoutineController 테스트")
class ChallengeRoutineControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ChallengeRoutineService challengeRoutineService;

	@MockitoBean
	private ChallengeCustomRoutineFacade challengeCustomRoutineFacade;

	@Nested
	@DisplayName("PATCH /api/challenges/routines/{routineId} - 루틴 완료 토글")
	class ToggleRoutineCompletion {

		@ParameterizedTest
		@CsvSource({
			"true",
			"false"
		})
		@DisplayName("성공 - 루틴 완료 상태 토글")
		void successToggle(boolean isComplete) throws Exception {
			// given
			RoutineCompletionResponseDto response = createMockRoutineCompletionResponse(isComplete);

			given(challengeRoutineService.toggleCompletion(DEFAULT_USER_ID, DEFAULT_ROUTINE_ID))
				.willReturn(response);

			// when & then
			mockMvc.perform(patch("/api/challenges/routines/{routineId}", DEFAULT_ROUTINE_ID)
					.header("X-User-Id", DEFAULT_USER_ID))
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
			mockMvc.perform(patch("/api/challenges/routines/{routineId}", invalidRoutineId)
					.header("X-User-Id", DEFAULT_USER_ID))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 다른 사용자의 루틴에 접근")
		void failUnauthorizedAccess() throws Exception {
			// given
			given(challengeRoutineService.toggleCompletion(DEFAULT_USER_ID, DEFAULT_ROUTINE_ID))
				.willThrow(new ChallengeException(ChallengeErrorCode.UNAUTHORIZED_ACCESS));

			// when & then
			mockMvc.perform(patch("/api/challenges/routines/{routineId}", DEFAULT_ROUTINE_ID)
					.header("X-User-Id", DEFAULT_USER_ID))
				.andExpect(status().isForbidden());
		}
	}

	@Nested
	@DisplayName("PATCH /api/challenges/routines - 루틴 일괄 업데이트")
	class UpdateMultipleRoutines {

		@Test
		@DisplayName("성공 - 여러 루틴 일괄 업데이트 (3개)")
		void successUpdateMultipleRoutines() throws Exception {
			// given
			RoutineUpdateRequestDto request = createValidRoutineUpdateRequest();
			RoutineBatchUpdateResponseDto response = createMockRoutineBatchUpdateResponse();

			given(challengeRoutineService.updateMultipleRoutines(eq(DEFAULT_USER_ID), any(RoutineUpdateRequestDto.class)))
				.willReturn(response);

			// when & then
			mockMvc.perform(patch("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.updatedCount").value(3))
				.andExpect(jsonPath("$.data.routines").isArray())
				.andExpect(jsonPath("$.data.routines.length()").value(3));
		}

		@Test
		@DisplayName("성공 - 단일 루틴 업데이트")
		void successUpdateSingleRoutine() throws Exception {
			// given
			RoutineUpdateRequestDto request = createSingleRoutineUpdateRequest();
			RoutineBatchUpdateResponseDto response = createMockSingleRoutineBatchUpdateResponse();

			given(challengeRoutineService.updateMultipleRoutines(eq(DEFAULT_USER_ID), any(RoutineUpdateRequestDto.class)))
				.willReturn(response);

			// when & then
			mockMvc.perform(patch("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.updatedCount").value(1))
				.andExpect(jsonPath("$.data.routines").isArray())
				.andExpect(jsonPath("$.data.routines.length()").value(1));
		}

		@Test
		@DisplayName("실패 - 빈 루틴 목록 (@NotEmpty 검증)")
		void failEmptyRoutineList() throws Exception {
			// given
			RoutineUpdateRequestDto request = createRoutineUpdateRequestWithEmptyList();

			// when & then
			mockMvc.perform(patch("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - null routineId (@NotNull 검증)")
		void failNullRoutineId() throws Exception {
			// given
			RoutineUpdateRequestDto request = createRoutineUpdateRequestWithNullRoutineId();

			// when & then
			mockMvc.perform(patch("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - null isComplete (@NotNull 검증)")
		void failNullIsComplete() throws Exception {
			// given
			RoutineUpdateRequestDto request = createRoutineUpdateRequestWithNullIsComplete();

			// when & then
			mockMvc.perform(patch("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 루틴")
		void failRoutineNotFound() throws Exception {
			// given
			RoutineUpdateRequestDto request = createValidRoutineUpdateRequest();

			given(challengeRoutineService.updateMultipleRoutines(eq(DEFAULT_USER_ID), any(RoutineUpdateRequestDto.class)))
				.willThrow(new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND));

			// when & then
			mockMvc.perform(patch("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 서로 다른 챌린지의 루틴")
		void failRoutinesFromDifferentChallenges() throws Exception {
			// given
			RoutineUpdateRequestDto request = createValidRoutineUpdateRequest();

			given(challengeRoutineService.updateMultipleRoutines(eq(DEFAULT_USER_ID), any(RoutineUpdateRequestDto.class)))
				.willThrow(new ChallengeException(ChallengeErrorCode.ROUTINES_FROM_DIFFERENT_CHALLENGES));

			// when & then
			mockMvc.perform(patch("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 소유자가 아님")
		void failUnauthorizedAccess() throws Exception {
			// given
			RoutineUpdateRequestDto request = createValidRoutineUpdateRequest();

			given(challengeRoutineService.updateMultipleRoutines(eq(DEFAULT_USER_ID), any(RoutineUpdateRequestDto.class)))
				.willThrow(new ChallengeException(ChallengeErrorCode.UNAUTHORIZED_ACCESS));

			// when & then
			mockMvc.perform(patch("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
		}
	}

	@Nested
	@DisplayName("POST /api/challenges/routines - 커스텀 루틴 추가")
	class CustomRoutineAddTest {

		@Test
		@DisplayName("성공 - 커스텀 루틴 추가")
		void successAddCustomRoutine() throws Exception {
			// given
			CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");
			CustomRoutineAddResponseDto response = createMockCustomRoutineAddResponse();

			given(challengeCustomRoutineFacade.addCustomRoutine(eq(DEFAULT_USER_ID), any(CustomRoutineAddRequestDto.class)))
				.willReturn(response);

			// when & then
			mockMvc.perform(post("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.challengeId").value(response.challengeId()))
				.andExpect(jsonPath("$.data.routineName").value("저녁 마사지"))
				.andExpect(jsonPath("$.data.totalRoutineCount").value(26));
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 사용자")
		void failUserNotFound() throws Exception {
			// given
			CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");

			given(challengeCustomRoutineFacade.addCustomRoutine(eq(DEFAULT_USER_ID), any(CustomRoutineAddRequestDto.class)))
				.willThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 활성 챌린지가 없을 때")
		void failNoChallengeFound() throws Exception {
			// given
			CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");

			given(challengeCustomRoutineFacade.addCustomRoutine(eq(DEFAULT_USER_ID), any(CustomRoutineAddRequestDto.class)))
				.willThrow(new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 챌린지 기간 외의 날짜")
		void failRoutineOutOfChallengePeriod() throws Exception {
			// given
			CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");

			given(challengeCustomRoutineFacade.addCustomRoutine(eq(DEFAULT_USER_ID), any(CustomRoutineAddRequestDto.class)))
				.willThrow(new ChallengeException(ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD));

			// when & then
			mockMvc.perform(post("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (빈 루틴명)")
		void failValidationEmptyRoutineName() throws Exception {
			// given
			CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("");

			// when & then
			mockMvc.perform(post("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (null 루틴명)")
		void failValidationNullRoutineName() throws Exception {
			// given
			String requestBody = "{\"routineName\": null}";

			// when & then
			mockMvc.perform(post("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestBody))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (루틴명 길이 초과)")
		void failValidationRoutineNameTooLong() throws Exception {
			// given - 101자 루틴명
			String longRoutineName = "a".repeat(101);
			CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto(longRoutineName);

			// when & then
			mockMvc.perform(post("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패 - 하루 루틴 개수 제한 초과")
		void failCustomRoutineLimitExceeded() throws Exception {
			// given
			CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("초과 루틴");

			given(challengeCustomRoutineFacade.addCustomRoutine(eq(DEFAULT_USER_ID), any(CustomRoutineAddRequestDto.class)))
				.willThrow(new ChallengeException(ChallengeErrorCode.CUSTOM_ROUTINE_LIMIT_EXCEEDED));

			// when & then
			mockMvc.perform(post("/api/challenges/routines")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CH011"));
		}
	}
}
