package com.sopt.cherrish.domain.procedure.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureRepository;
import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureWorryRepository;
import com.sopt.cherrish.domain.procedure.fixture.ProcedureFixture;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureListResponseDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcedureService 단위 테스트")
class ProcedureServiceTest {

	@InjectMocks
	private ProcedureService procedureService;

	@Mock
	private ProcedureRepository procedureRepository;

	@Mock
	private ProcedureWorryRepository procedureWorryRepository;

	@Test
	@DisplayName("시술 검색 성공 - keyword, worryId 둘 다 null")
	void searchProceduresWithNoFilters() {
		// given
        Procedure procedure1 = ProcedureFixture.createProcedure("필러", "주사", 1, 3);
        Procedure procedure2 = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);

        List<Procedure> procedures = Arrays.asList(procedure1, procedure2);
		given(procedureRepository.searchProcedures(null, null)).willReturn(procedures);
		given(procedureWorryRepository.findAllByProcedureIdInWithWorry(any())).willReturn(Collections.emptyList());

		// when
		ProcedureListResponseDto result = procedureService.searchProcedures(null, null);

		// then
		assertThat(result.getProcedures()).hasSize(2);
        // 이름순 정렬 확인
		assertThat(result.getProcedures().get(0).getName()).isEqualTo("레이저 토닝");
		assertThat(result.getProcedures().get(1).getName()).isEqualTo("필러");
	}

	@Test
	@DisplayName("시술 검색 성공 - 키워드로 검색")
	void searchProceduresByKeyword() {
		// given
		String keyword = "레이저";
		Procedure procedure = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);

		given(procedureRepository.searchProcedures(keyword, null)).willReturn(Collections.singletonList(procedure));
		given(procedureWorryRepository.findAllByProcedureIdInWithWorry(any())).willReturn(Collections.emptyList());

		// when
		ProcedureListResponseDto result = procedureService.searchProcedures(keyword, null);

		// then
		assertThat(result.getProcedures()).hasSize(1);
		assertThat(result.getProcedures().get(0).getName()).isEqualTo("레이저 토닝");
		assertThat(result.getProcedures().get(0).getWorries()).isEmpty();
	}

	@Test
	@DisplayName("시술 검색 성공 - 피부 고민 ID로 검색")
	void searchProceduresByWorryId() {
		// given
		Long worryId = 1L;
		Procedure procedure = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);

		given(procedureRepository.searchProcedures(null, worryId)).willReturn(Collections.singletonList(procedure));
		given(procedureWorryRepository.findAllByProcedureIdInWithWorry(any())).willReturn(Collections.emptyList());

		// when
		ProcedureListResponseDto result = procedureService.searchProcedures(null, worryId);

		// then
		assertThat(result.getProcedures()).hasSize(1);
		assertThat(result.getProcedures().get(0).getName()).isEqualTo("레이저 토닝");
	}

    @Test
    @DisplayName("시술 검색 성공 - 키워드와 피부 고민 ID 동시 검색")
    void searchProceduresWithBothFilters() {
        // given
        String keyword = "레이저";
        Long worryId = 1L;
        Procedure procedure = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);

        given(procedureRepository.searchProcedures(keyword, worryId))
                .willReturn(Collections.singletonList(procedure));
		given(procedureWorryRepository.findAllByProcedureIdInWithWorry(any())).willReturn(Collections.emptyList());

        // when
        ProcedureListResponseDto result = procedureService.searchProcedures(keyword, worryId);

        // then
        assertThat(result.getProcedures()).hasSize(1);
        assertThat(result.getProcedures().get(0).getName()).isEqualTo("레이저 토닝");
        assertThat(result.getProcedures().get(0).getWorries()).isEmpty();
    }

    @Test
    @DisplayName("시술 검색 성공 - 빈 문자열 키워드 처리")
    void searchProceduresWithEmptyKeyword() {
        // given
        String emptyKeyword = "";
        given(procedureRepository.searchProcedures(emptyKeyword, null))
                .willReturn(Collections.emptyList());

        // when
        ProcedureListResponseDto result = procedureService.searchProcedures(emptyKeyword, null);

        // then
        assertThat(result.getProcedures()).isEmpty();
    }

	@Test
	@DisplayName("시술 검색 성공 - 결과가 없는 경우")
	void searchProceduresWithNoResults() {
		// given
		String keyword = "존재하지않는시술";
		given(procedureRepository.searchProcedures(keyword, null)).willReturn(Collections.emptyList());

		// when
		ProcedureListResponseDto result = procedureService.searchProcedures(keyword, null);

		// then
		assertThat(result.getProcedures()).isEmpty();
	}

	@Test
	@DisplayName("시술 검색 성공 - 응답 DTO 변환 확인")
	void searchProceduresCheckDtoConversion() {
		// given
		Procedure procedure = ProcedureFixture.createProcedure("프락셀 레이저", "레이저", 3, 7);

		given(procedureRepository.searchProcedures(null, null))
			.willReturn(Collections.singletonList(procedure));
		given(procedureWorryRepository.findAllByProcedureIdInWithWorry(any())).willReturn(Collections.emptyList());

		// when
		ProcedureListResponseDto result = procedureService.searchProcedures(null, null);

		// then
		assertThat(result.getProcedures()).hasSize(1);
		assertThat(result.getProcedures().get(0).getId()).isNotNull();
		assertThat(result.getProcedures().get(0).getName()).isEqualTo("프락셀 레이저");
		assertThat(result.getProcedures().get(0).getWorries()).isEmpty();
		assertThat(result.getProcedures().get(0).getMinDowntimeDays()).isEqualTo(3);
		assertThat(result.getProcedures().get(0).getMaxDowntimeDays()).isEqualTo(7);
	}
}
