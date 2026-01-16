package com.sopt.cherrish.domain.challenge.core.presentation;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_CHALLENGE_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_CHALLENGE_TITLE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_USER_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createMockChallengeCreateResponse;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRequestWithEmptyRoutines;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createRequestWithTooManyRoutines;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createValidChallengeRequest;
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
import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeCreationFacade;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;

@WebMvcTest(ChallengeLifecycleController.class)
@DisplayName("ChallengeLifecycleController 테스트")
class ChallengeLifecycleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ChallengeCreationFacade challengeCreationFacade;

	@Nested
	@DisplayName("POST /api/challenges - 챌린지 생성")
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
			mockMvc.perform(post("/api/challenges")
					.header("X-User-Id", DEFAULT_USER_ID)
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
			mockMvc.perform(post("/api/challenges")
					.header("X-User-Id", invalidUserId)
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
			mockMvc.perform(post("/api/challenges")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
		}

	}

	@Nested
	@DisplayName("유효성 검증 테스트")
	class ValidationTests {

		@Test
		@DisplayName("실패 - 빈 루틴 목록")
		void failEmptyRoutines() throws Exception {
			// given
			ChallengeCreateRequestDto request = createRequestWithEmptyRoutines();

			// when & then
			mockMvc.perform(post("/api/challenges")
					.header("X-User-Id", DEFAULT_USER_ID)
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
			mockMvc.perform(post("/api/challenges")
					.header("X-User-Id", DEFAULT_USER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}
	}
}
