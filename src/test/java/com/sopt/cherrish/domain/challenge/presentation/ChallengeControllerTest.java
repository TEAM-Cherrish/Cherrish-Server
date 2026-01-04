package com.sopt.cherrish.domain.challenge.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.challenge.application.dto.response.HomecareRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.application.service.AiChallengeRecommendationService;
import com.sopt.cherrish.domain.challenge.application.service.HomecareRoutineService;
import com.sopt.cherrish.domain.challenge.presentation.dto.request.AiRecommendationRequestDto;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.AiRecommendationResponseDto;

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

	@Test
	@DisplayName("홈케어 루틴 목록 조회 성공")
	void getHomecareRoutinesSuccess() throws Exception {
		// given
		List<HomecareRoutineResponseDto> mockRoutines = List.of(
			new HomecareRoutineResponseDto(1, "SKIN_MOISTURIZING", "피부 보습 관리"),
			new HomecareRoutineResponseDto(2, "SKIN_BRIGHTENING", "피부 미백 관리"),
			new HomecareRoutineResponseDto(3, "WRINKLE_CARE", "주름 개선 관리")
		);

		given(homecareRoutineService.getAllHomecareRoutines()).willReturn(mockRoutines);

		// when & then
		mockMvc.perform(get("/api/challenges/homecare-routines"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[0].id").value(1))
			.andExpect(jsonPath("$.data[0].name").value("SKIN_MOISTURIZING"))
			.andExpect(jsonPath("$.data[0].description").value("피부 보습 관리"))
			.andExpect(jsonPath("$.data[1].id").value(2))
			.andExpect(jsonPath("$.data[2].id").value(3));
	}

	@Test
	@DisplayName("홈케어 루틴 목록 조회 성공 - 빈 목록")
	void getHomecareRoutinesEmpty() throws Exception {
		// given
		given(homecareRoutineService.getAllHomecareRoutines()).willReturn(List.of());

		// when & then
		mockMvc.perform(get("/api/challenges/homecare-routines"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 성공")
	void generateAiRecommendationSuccess() throws Exception {
		// given
		AiRecommendationRequestDto request = new AiRecommendationRequestDto(1L);
		AiRecommendationResponseDto response = AiRecommendationResponseDto.of(
			"피부 보습 7일 챌린지",
			List.of("아침 세안 후 토너 바르기", "저녁 보습 크림 바르기", "하루 8잔 물 마시기")
		);

		given(aiRecommendationService.generateRecommendation(any(Long.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/api/challenges/ai-recommendations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.challengeTitle").value("피부 보습 7일 챌린지"))
			.andExpect(jsonPath("$.data.routines").isArray())
			.andExpect(jsonPath("$.data.routines.length()").value(3))
			.andExpect(jsonPath("$.data.routines[0]").value("아침 세안 후 토너 바르기"));
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 성공 - 다양한 루틴 ID")
	void generateAiRecommendationWithDifferentRoutineId() throws Exception {
		// given
		AiRecommendationRequestDto request = new AiRecommendationRequestDto(3L);
		AiRecommendationResponseDto response = AiRecommendationResponseDto.of(
			"주름 개선 7일 챌린지",
			List.of("레티놀 세럼 바르기", "충분한 수면")
		);

		given(aiRecommendationService.generateRecommendation(3L))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/api/challenges/ai-recommendations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.challengeTitle").value("주름 개선 7일 챌린지"))
			.andExpect(jsonPath("$.data.routines.length()").value(2));
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 실패 - 요청 본문 없음")
	void generateAiRecommendationNoRequestBody() throws Exception {
		// when & then
		mockMvc.perform(post("/api/challenges/ai-recommendations")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 실패 - 잘못된 JSON 형식")
	void generateAiRecommendationInvalidJson() throws Exception {
		// when & then
		mockMvc.perform(post("/api/challenges/ai-recommendations")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{invalid json}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 실패 - null homecareRoutineId")
	void generateAiRecommendationNullRoutineId() throws Exception {
		// given
		String requestBody = "{\"homecareRoutineId\": null}";

		// when & then
		mockMvc.perform(post("/api/challenges/ai-recommendations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 성공 - 빈 루틴 리스트")
	void generateAiRecommendationWithEmptyRoutines() throws Exception {
		// given
		AiRecommendationRequestDto request = new AiRecommendationRequestDto(1L);
		AiRecommendationResponseDto response = AiRecommendationResponseDto.of(
			"테스트 챌린지",
			List.of()
		);

		given(aiRecommendationService.generateRecommendation(any(Long.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/api/challenges/ai-recommendations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.challengeTitle").value("테스트 챌린지"))
			.andExpect(jsonPath("$.data.routines").isArray())
			.andExpect(jsonPath("$.data.routines.length()").value(0));
	}
}
