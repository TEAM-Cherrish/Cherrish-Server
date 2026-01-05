package com.sopt.cherrish.domain.challenge.core.presentation;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeRoutineResponseDto;
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
}
