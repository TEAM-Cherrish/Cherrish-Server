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
			new WorryResponseDto(1L, "여드름/트러블"),
			new WorryResponseDto(2L, "색소/잡티"),
			new WorryResponseDto(3L, "홍조"),
			new WorryResponseDto(4L, "탄력/주름"),
			new WorryResponseDto(5L, "모공"),
			new WorryResponseDto(6L, "피부결/각질")
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
			.andExpect(jsonPath("$.data[0].content").value("여드름/트러블"));
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
