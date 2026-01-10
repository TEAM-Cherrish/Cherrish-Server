package com.sopt.cherrish.domain.challenge.core.presentation;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_CHALLENGE_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_USER_ID;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.createMockChallengeDetailResponse;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sopt.cherrish.domain.challenge.core.application.facade.ChallengeQueryFacade;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;

@WebMvcTest(ChallengeQueryController.class)
@DisplayName("ChallengeQueryController 테스트")
class ChallengeQueryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ChallengeQueryFacade challengeQueryFacade;

	@Nested
	@DisplayName("GET /api/challenges - 활성 챌린지 조회")
	class GetActiveChallenge {

		@Test
		@DisplayName("성공 - 활성 챌린지 상세 조회")
		void success() throws Exception {
			// given
			ChallengeDetailResponseDto response = createMockChallengeDetailResponse();

			given(challengeQueryFacade.getActiveChallengeDetail(DEFAULT_USER_ID))
				.willReturn(response);

			// when & then
			mockMvc.perform(get("/api/challenges")
					.header("X-User-Id", DEFAULT_USER_ID))
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
			mockMvc.perform(get("/api/challenges")
					.header("X-User-Id", DEFAULT_USER_ID))
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
			mockMvc.perform(get("/api/challenges")
					.header("X-User-Id", invalidUserId))
				.andExpect(status().isNotFound());
		}
	}
}
