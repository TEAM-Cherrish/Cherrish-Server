package com.sopt.cherrish.domain.procedure.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sopt.cherrish.domain.procedure.application.service.ProcedureService;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureListResponseDto;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureResponseDto;

@WebMvcTest(ProcedureController.class)
@DisplayName("ProcedureController 통합 테스트")
class ProcedureControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProcedureService procedureService;

	@Test
	@DisplayName("시술 목록 조회 성공 - 파라미터 없이 전체 조회")
	void searchProceduresWithoutParameters() throws Exception {
		// given
		ProcedureResponseDto procedure1 = ProcedureResponseDto.builder()
			.id(1L)
			.name("레이저 토닝")
			.worries(Collections.emptyList())
			.minDowntimeDays(0)
			.maxDowntimeDays(1)
			.build();

		ProcedureResponseDto procedure2 = ProcedureResponseDto.builder()
			.id(2L)
			.name("필러")
			.worries(Collections.emptyList())
			.minDowntimeDays(1)
			.maxDowntimeDays(3)
			.build();

		ProcedureListResponseDto response = ProcedureListResponseDto.of(Arrays.asList(procedure1, procedure2));

		given(procedureService.searchProcedures(null, null)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/procedures"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.procedures").isArray())
			.andExpect(jsonPath("$.data.procedures.length()").value(2))
			.andExpect(jsonPath("$.data.procedures[0].name").value("레이저 토닝"))
			.andExpect(jsonPath("$.data.procedures[1].name").value("필러"));
	}

	@Test
	@DisplayName("시술 목록 조회 성공 - 키워드로 검색")
	void searchProceduresByKeyword() throws Exception {
		// given
		String keyword = "레이저";

		ProcedureResponseDto procedure = ProcedureResponseDto.builder()
			.id(1L)
			.name("레이저 토닝")
			.worries(Collections.emptyList())
			.minDowntimeDays(0)
			.maxDowntimeDays(1)
			.build();

		ProcedureListResponseDto response = ProcedureListResponseDto.of(Collections.singletonList(procedure));

		given(procedureService.searchProcedures(any(), any())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/procedures")
				.param("keyword", keyword))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.procedures").isArray())
			.andExpect(jsonPath("$.data.procedures.length()").value(1))
			.andExpect(jsonPath("$.data.procedures[0].name").value("레이저 토닝"))
			.andExpect(jsonPath("$.data.procedures[0].worries").isArray());
	}

	@Test
	@DisplayName("시술 목록 조회 성공 - 피부 고민 ID로 검색")
	void searchProceduresByWorryId() throws Exception {
		// given
		Long worryId = 1L;

		ProcedureResponseDto procedure = ProcedureResponseDto.builder()
			.id(1L)
			.name("레이저 토닝")
			.worries(Collections.emptyList())
			.minDowntimeDays(0)
			.maxDowntimeDays(1)
			.build();

		ProcedureListResponseDto response = ProcedureListResponseDto.of(Collections.singletonList(procedure));

		given(procedureService.searchProcedures(any(), any())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/procedures")
				.param("worryId", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.procedures").isArray())
			.andExpect(jsonPath("$.data.procedures.length()").value(1))
			.andExpect(jsonPath("$.data.procedures[0].name").value("레이저 토닝"));
	}

	@Test
	@DisplayName("시술 목록 조회 성공 - 검색 결과 없음")
	void searchProceduresWithNoResults() throws Exception {
		// given
		String keyword = "존재하지않는시술";

		ProcedureListResponseDto response = ProcedureListResponseDto.of(Collections.emptyList());

		given(procedureService.searchProcedures(any(), any())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/procedures")
				.param("keyword", keyword))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.procedures").isArray())
			.andExpect(jsonPath("$.data.procedures.length()").value(0));
	}

	@Test
	@DisplayName("시술 목록 조회 성공 - 응답 필드 검증")
	void searchProceduresCheckResponseFields() throws Exception {
		// given
		ProcedureResponseDto procedure = ProcedureResponseDto.builder()
			.id(2L)
			.name("프락셀 레이저")
			.worries(Collections.emptyList())
			.minDowntimeDays(3)
			.maxDowntimeDays(7)
			.build();

		ProcedureListResponseDto response = ProcedureListResponseDto.of(Collections.singletonList(procedure));

		given(procedureService.searchProcedures(null, null)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/procedures"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.procedures[0].id").value(2))
			.andExpect(jsonPath("$.data.procedures[0].name").value("프락셀 레이저"))
			.andExpect(jsonPath("$.data.procedures[0].worries").isArray())
			.andExpect(jsonPath("$.data.procedures[0].minDowntimeDays").value(3))
			.andExpect(jsonPath("$.data.procedures[0].maxDowntimeDays").value(7));
	}
}
