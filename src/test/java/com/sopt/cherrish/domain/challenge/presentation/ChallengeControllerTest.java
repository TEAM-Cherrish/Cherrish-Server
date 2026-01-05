package com.sopt.cherrish.domain.challenge.presentation;

import static com.sopt.cherrish.domain.challenge.fixture.ChallengeTestFixture.emptyRoutinesRecommendation;
import static com.sopt.cherrish.domain.challenge.fixture.ChallengeTestFixture.homecareRoutineList;
import static com.sopt.cherrish.domain.challenge.fixture.ChallengeTestFixture.recommendationRequest;
import static com.sopt.cherrish.domain.challenge.fixture.ChallengeTestFixture.skinMoisturizingRecommendation;
import static com.sopt.cherrish.domain.challenge.fixture.ChallengeTestFixture.wrinkleCareRecommendation;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.sopt.cherrish.domain.challenge.core.presentation.ChallengeController;
import com.sopt.cherrish.domain.challenge.homecare.application.service.HomecareRoutineService;
import com.sopt.cherrish.domain.challenge.recommendation.application.service.AiChallengeRecommendationService;

@WebMvcTest(ChallengeController.class)
@DisplayName("ChallengeController 통합 테스트")
class ChallengeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private HomecareRoutineService homecareRoutineService;

	@MockitoBean
	private AiChallengeRecommendationService aiRecommendationService;

	@MockitoBean
	private ChallengeCreationFacade challengeCreationFacade;

	@Nested
	@DisplayName("GET /api/challenges/homecare-routines - 홈케어 루틴 목록 조회")
	class GetHomecareRoutines {

		@Test
		@DisplayName("성공 - 루틴 목록 반환")
		void success() throws Exception {
			// given
			given(homecareRoutineService.getAllHomecareRoutines()).willReturn(homecareRoutineList());

			// when & then
			mockMvc.perform(get("/api/challenges/homecare-routines"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(6))
				.andExpect(jsonPath("$.data[0].id").value(1))
				.andExpect(jsonPath("$.data[0].name").value("SKIN_MOISTURIZING"))
				.andExpect(jsonPath("$.data[0].description").value("피부 보습 관리"))
				.andExpect(jsonPath("$.data[1].id").value(2))
				.andExpect(jsonPath("$.data[2].id").value(3))
				.andExpect(jsonPath("$.data[5].id").value(6));
		}

		@Test
		@DisplayName("성공 - 빈 목록 반환")
		void emptyList() throws Exception {
			// given
			given(homecareRoutineService.getAllHomecareRoutines()).willReturn(List.of());

			// when & then
			mockMvc.perform(get("/api/challenges/homecare-routines"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(0));
		}
	}

	@Nested
	@DisplayName("POST /api/challenges/ai-recommendations - AI 챌린지 추천 생성")
	class GenerateAiRecommendation {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("피부 보습 루틴 추천 생성")
			void skinMoisturizing() throws Exception {
				// given
				given(aiRecommendationService.generateRecommendation(1))
					.willReturn(skinMoisturizingRecommendation());

				// when & then
				mockMvc.perform(post("/api/challenges/ai-recommendations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(recommendationRequest(1))))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.challengeTitle").value("피부 보습 7일 챌린지"))
					.andExpect(jsonPath("$.data.routines").isArray())
					.andExpect(jsonPath("$.data.routines.length()").value(3))
					.andExpect(jsonPath("$.data.routines[0]").value("아침 세안 후 토너 바르기"));
			}

			@Test
			@DisplayName("주름 개선 루틴 추천 생성")
			void wrinkleCare() throws Exception {
				// given
				given(aiRecommendationService.generateRecommendation(3))
					.willReturn(wrinkleCareRecommendation());

				// when & then
				mockMvc.perform(post("/api/challenges/ai-recommendations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(recommendationRequest(3))))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.challengeTitle").value("주름 개선 7일 챌린지"))
					.andExpect(jsonPath("$.data.routines.length()").value(2));
			}

			@Test
			@DisplayName("빈 루틴 리스트 추천 생성")
			void emptyRoutines() throws Exception {
				// given
				given(aiRecommendationService.generateRecommendation(1))
					.willReturn(emptyRoutinesRecommendation());

				// when & then
				mockMvc.perform(post("/api/challenges/ai-recommendations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(recommendationRequest(1))))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.challengeTitle").value("테스트 챌린지"))
					.andExpect(jsonPath("$.data.routines").isArray())
					.andExpect(jsonPath("$.data.routines.length()").value(0));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Failure {
			@Test
			@DisplayName("null homecareRoutineId")
			void nullRoutineId() throws Exception {
				// given
				String requestBody = "{\"homecareRoutineId\": null}";

				// when & then
				mockMvc.perform(post("/api/challenges/ai-recommendations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
					.andExpect(status().isBadRequest());
			}
		}
	}
}
