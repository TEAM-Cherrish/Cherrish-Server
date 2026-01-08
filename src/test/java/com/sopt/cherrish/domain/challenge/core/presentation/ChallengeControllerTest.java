package com.sopt.cherrish.domain.challenge.core.presentation;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createChallengeResponse;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createDefaultChallenge;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRequestWithEmptyTitle;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createValidChallengeRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeCreationFacade;
import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeQueryFacade;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;

import java.time.LocalDate;

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
			Long userId = 1L;
			Long challengeId = 1L;
			ChallengeCreateRequestDto request = createValidChallengeRequest();

			// Response 생성 (Mock 테스트용)
			Challenge challenge = createDefaultChallenge(userId);
			List<ChallengeRoutine> routines = challenge.createChallengeRoutines(request.routineNames());

			ChallengeCreateResponseDto response = createChallengeResponse(challenge, routines, challengeId);

			given(challengeCreationFacade.createChallenge(eq(userId), any(ChallengeCreateRequestDto.class)))
				.willReturn(response);

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.challengeId").value(1))
				.andExpect(jsonPath("$.data.title").value("7일 챌린지"))
				.andExpect(jsonPath("$.data.totalDays").value(7))
				.andExpect(jsonPath("$.data.totalRoutineCount").value(21))
				.andExpect(jsonPath("$.data.routines").isArray())
				.andExpect(jsonPath("$.data.routines[0].name").value("아침 세안"))
				.andExpect(jsonPath("$.data.routines[0].isComplete").value(false));
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 사용자")
		void failUserNotFound() throws Exception {
			// given
			Long userId = 999L;
			ChallengeCreateRequestDto request = createValidChallengeRequest();

			given(challengeCreationFacade.createChallenge(eq(userId), any(ChallengeCreateRequestDto.class)))
				.willThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 이미 활성 챌린지가 존재함")
		void failDuplicateActiveChallenge() throws Exception {
			// given
			Long userId = 1L;
			ChallengeCreateRequestDto request = createValidChallengeRequest();

			given(challengeCreationFacade.createChallenge(eq(userId), any(ChallengeCreateRequestDto.class)))
				.willThrow(new ChallengeException(ChallengeErrorCode.DUPLICATE_ACTIVE_CHALLENGE));

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
		}

		@Test
		@DisplayName("실패 - 유효성 검증 실패 (빈 제목)")
		void failValidation() throws Exception {
			// given
			Long userId = 1L;
			ChallengeCreateRequestDto request = createRequestWithEmptyTitle();

			// when & then
			mockMvc.perform(post("/api/challenges/{userId}", userId)
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
			Long userId = 1L;
			Long challengeId = 1L;

			// Mock response 생성
			List<ChallengeRoutineResponseDto> todayRoutines = List.of(
				new ChallengeRoutineResponseDto(1L, "아침 세안", LocalDate.of(2024, 1, 1), false),
				new ChallengeRoutineResponseDto(2L, "토너 바르기", LocalDate.of(2024, 1, 1), true),
				new ChallengeRoutineResponseDto(3L, "크림 바르기", LocalDate.of(2024, 1, 1), false)
			);

			ChallengeDetailResponseDto response = new ChallengeDetailResponseDto(
				challengeId,
				"7일 보습 챌린지",
				3,
				42.5,
				2,
				50.0,
				todayRoutines,
				"3일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!"
			);

			given(challengeQueryFacade.getActiveChallengeDetail(userId))
				.willReturn(response);

			// when & then
			mockMvc.perform(get("/api/challenges/{userId}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.challengeId").value(1))
				.andExpect(jsonPath("$.data.title").value("7일 보습 챌린지"))
				.andExpect(jsonPath("$.data.currentDay").value(3))
				.andExpect(jsonPath("$.data.progressPercentage").value(42.5))
				.andExpect(jsonPath("$.data.cherryLevel").value(2))
				.andExpect(jsonPath("$.data.progressToNextLevel").value(50.0))
				.andExpect(jsonPath("$.data.todayRoutines").isArray())
				.andExpect(jsonPath("$.data.todayRoutines.length()").value(3))
				.andExpect(jsonPath("$.data.todayRoutines[0].routineId").value(1))
				.andExpect(jsonPath("$.data.todayRoutines[0].name").value("아침 세안"))
				.andExpect(jsonPath("$.data.todayRoutines[0].isComplete").value(false))
				.andExpect(jsonPath("$.data.todayRoutines[1].isComplete").value(true))
				.andExpect(jsonPath("$.data.cheeringMessage").value("3일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!"));
		}

		@Test
		@DisplayName("실패 - 활성 챌린지가 없을 때")
		void failNoChallengeFound() throws Exception {
			// given
			Long userId = 1L;

			given(challengeQueryFacade.getActiveChallengeDetail(userId))
				.willThrow(new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

			// when & then
			mockMvc.perform(get("/api/challenges/{userId}", userId))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 사용자")
		void failUserNotFound() throws Exception {
			// given
			Long userId = 999L;

			given(challengeQueryFacade.getActiveChallengeDetail(userId))
				.willThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

			// when & then
			mockMvc.perform(get("/api/challenges/{userId}", userId))
				.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("PATCH /api/challenges/{userId}/routines/{routineId} - 루틴 완료 토글")
	class ToggleRoutineCompletion {

		@Test
		@DisplayName("성공 - 루틴 완료 상태 토글 (미완료 → 완료)")
		void successToggleToComplete() throws Exception {
			// given
			Long userId = 1L;
			Long routineId = 1L;

			RoutineCompletionResponseDto response = new RoutineCompletionResponseDto(
				routineId,
				"아침 세안",
				true,
				"루틴을 완료했습니다!"
			);

			given(challengeRoutineService.toggleCompletion(userId, routineId))
				.willReturn(response);

			// when & then
			mockMvc.perform(patch("/api/challenges/{userId}/routines/{routineId}", userId, routineId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.routineId").value(1))
				.andExpect(jsonPath("$.data.name").value("아침 세안"))
				.andExpect(jsonPath("$.data.isComplete").value(true))
				.andExpect(jsonPath("$.data.message").value("루틴을 완료했습니다!"));
		}

		@Test
		@DisplayName("성공 - 루틴 완료 상태 토글 (완료 → 미완료)")
		void successToggleToIncomplete() throws Exception {
			// given
			Long userId = 1L;
			Long routineId = 1L;

			RoutineCompletionResponseDto response = new RoutineCompletionResponseDto(
				routineId,
				"아침 세안",
				false,
				"루틴 완료를 취소했습니다."
			);

			given(challengeRoutineService.toggleCompletion(userId, routineId))
				.willReturn(response);

			// when & then
			mockMvc.perform(patch("/api/challenges/{userId}/routines/{routineId}", userId, routineId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.routineId").value(1))
				.andExpect(jsonPath("$.data.name").value("아침 세안"))
				.andExpect(jsonPath("$.data.isComplete").value(false))
				.andExpect(jsonPath("$.data.message").value("루틴 완료를 취소했습니다."));
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 루틴")
		void failRoutineNotFound() throws Exception {
			// given
			Long userId = 1L;
			Long routineId = 999L;

			given(challengeRoutineService.toggleCompletion(userId, routineId))
				.willThrow(new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND));

			// when & then
			mockMvc.perform(patch("/api/challenges/{userId}/routines/{routineId}", userId, routineId))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("실패 - 다른 사용자의 루틴에 접근")
		void failUnauthorizedAccess() throws Exception {
			// given
			Long userId = 1L;
			Long routineId = 1L;

			given(challengeRoutineService.toggleCompletion(userId, routineId))
				.willThrow(new ChallengeException(ChallengeErrorCode.UNAUTHORIZED_ACCESS));

			// when & then
			mockMvc.perform(patch("/api/challenges/{userId}/routines/{routineId}", userId, routineId))
				.andExpect(status().isForbidden());
		}
	}
}
