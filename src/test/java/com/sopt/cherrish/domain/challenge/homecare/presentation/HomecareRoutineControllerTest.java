package com.sopt.cherrish.domain.challenge.homecare.presentation;

import static com.sopt.cherrish.domain.challenge.fixture.ChallengeTestFixture.homecareRoutineList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.challenge.homecare.application.service.HomecareRoutineService;

@WebMvcTest(HomecareRoutineController.class)
@DisplayName("HomecareRoutineController 통합 테스트")
class HomecareRoutineControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private HomecareRoutineService homecareRoutineService;

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
}
