package com.sopt.cherrish.domain.challenge.homecare.presentation;

import static com.sopt.cherrish.domain.challenge.homecare.fixture.HomecareRoutineTestFixture.homecareRoutineList;
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

import com.sopt.cherrish.domain.challenge.homecare.application.service.HomecareRoutineService;
import com.sopt.cherrish.domain.challenge.homecare.presentation.dto.response.HomecareRoutineResponseDto;

@WebMvcTest(HomecareRoutineController.class)
@DisplayName("HomecareRoutineController 통합 테스트")
class HomecareRoutineControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private HomecareRoutineService homecareRoutineService;

	@Nested
	@DisplayName("GET /api/challenges/homecare-routines - 홈케어 루틴 목록 조회")
	class GetHomecareRoutines {

		@Test
		@DisplayName("성공 - 루틴 목록 반환")
		void success() throws Exception {
			// given
			List<HomecareRoutineResponseDto> expectedRoutines = homecareRoutineList();
			given(homecareRoutineService.getAllHomecareRoutines()).willReturn(expectedRoutines);

			// when & then
			mockMvc.perform(get("/api/challenges/homecare-routines"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(expectedRoutines.size()))
				// 대표 요소 하나는 fixture와 비교하며 완전히 검증
				.andExpect(jsonPath("$.data[0].id").value(expectedRoutines.get(0).id()))
				.andExpect(jsonPath("$.data[0].name").value(expectedRoutines.get(0).name()))
				.andExpect(jsonPath("$.data[0].description").value(expectedRoutines.get(0).description()))
				// 모든 요소가 필수 필드를 가지는지 검증
				.andExpect(jsonPath("$.data[*].id").exists())
				.andExpect(jsonPath("$.data[*].name").exists())
				.andExpect(jsonPath("$.data[*].description").exists());
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
