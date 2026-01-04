package com.sopt.cherrish.domain.challenge.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.challenge.application.dto.response.HomecareRoutineResponseDto;

@DisplayName("HomecareRoutineService 단위 테스트")
class HomecareRoutineServiceTest {

	private final HomecareRoutineService homecareRoutineService = new HomecareRoutineService();

	@Test
	@DisplayName("모든 홈케어 루틴 조회 - 기본 검증")
	void getAllHomecareRoutinesBasicValidation() {
		// when
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();

		// then
		assertThat(routines)
			.isNotNull()
			.hasSize(6)
			.allSatisfy(routine -> {
				assertThat(routine.id()).isBetween(1, 6);
				assertThat(routine.name()).isNotBlank();
				assertThat(routine.description()).isNotBlank();
			});
	}

	@Test
	@DisplayName("모든 홈케어 루틴 조회 - ID 순차 증가 검증")
	void getAllHomecareRoutinesIdSequential() {
		// when
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();

		// then
		assertThat(routines)
			.extracting(HomecareRoutineResponseDto::id)
			.containsExactly(1, 2, 3, 4, 5, 6);
	}

	@Test
	@DisplayName("모든 홈케어 루틴 조회 - DTO 매핑 정확성 검증")
	void getAllHomecareRoutinesDtoMapping() {
		// when
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();

		// then
		assertThat(routines.getFirst())
			.satisfies(routine -> {
				assertThat(routine.id()).isEqualTo(1);
				assertThat(routine.name()).isEqualTo("SKIN_MOISTURIZING");
				assertThat(routine.description()).isEqualTo("피부 보습 관리");
			});
	}
}
