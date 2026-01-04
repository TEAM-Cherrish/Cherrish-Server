package com.sopt.cherrish.domain.worry.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sopt.cherrish.domain.worry.application.service.WorryService;
import com.sopt.cherrish.domain.worry.presentation.dto.response.WorryResponseDto;

@WebMvcTest(WorryController.class)
@DisplayName("WorryController 통합 테스트")
class WorryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private WorryService worryService;

	@Test
	@DisplayName("피부 고민 전체 조회 성공")
	void getAllWorriesSuccess() throws Exception {
		// given
		List<WorryResponseDto> response = Arrays.asList(
			WorryResponseDto.builder().id(1L).content("여드름/트러블").build(),
			WorryResponseDto.builder().id(2L).content("색소/잡티").build(),
			WorryResponseDto.builder().id(3L).content("홍조").build(),
			WorryResponseDto.builder().id(4L).content("탄력/주름").build(),
			WorryResponseDto.builder().id(5L).content("모공").build(),
			WorryResponseDto.builder().id(6L).content("피부결/각질").build()
		);

		given(worryService.getAllWorries()).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/worries"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("S200"))
			.andExpect(jsonPath("$.message").value("성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data.length()").value(6))
			.andExpect(jsonPath("$.data[0].id").value(1))
			.andExpect(jsonPath("$.data[0].content").value("여드름/트러블"))
			.andExpect(jsonPath("$.data[1].id").value(2))
			.andExpect(jsonPath("$.data[1].content").value("색소/잡티"))
			.andExpect(jsonPath("$.data[2].id").value(3))
			.andExpect(jsonPath("$.data[2].content").value("홍조"))
			.andExpect(jsonPath("$.data[3].id").value(4))
			.andExpect(jsonPath("$.data[3].content").value("탄력/주름"))
			.andExpect(jsonPath("$.data[4].id").value(5))
			.andExpect(jsonPath("$.data[4].content").value("모공"))
			.andExpect(jsonPath("$.data[5].id").value(6))
			.andExpect(jsonPath("$.data[5].content").value("피부결/각질"));
	}

	@Test
	@DisplayName("피부 고민 전체 조회 - 빈 리스트")
	void getAllWorriesEmpty() throws Exception {
		// given
		given(worryService.getAllWorries()).willReturn(List.of());

		// when & then
		mockMvc.perform(get("/api/worries"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("S200"))
			.andExpect(jsonPath("$.message").value("성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data.length()").value(0));
	}
}
