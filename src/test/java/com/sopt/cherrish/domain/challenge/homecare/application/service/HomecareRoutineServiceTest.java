package com.sopt.cherrish.domain.challenge.homecare.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.challenge.homecare.presentation.dto.response.HomecareRoutineResponseDto;

@DisplayName("HomecareRoutineService 단위 테스트")
class HomecareRoutineServiceTest {

	private final HomecareRoutineService homecareRoutineService = new HomecareRoutineService();

	@Test
	@DisplayName("모든 홈케어 루틴 조회 - 기본 검증")
	void getAllHomecareRoutinesBasicValidation() {
		// when
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();

		// then
		int enumCount = HomecareRoutine.values().length;
		assertThat(routines)
			.isNotNull()
			.hasSize(enumCount)
			.allSatisfy(routine -> {
				assertThat(routine.id()).isBetween(1, enumCount);
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
		Integer[] expectedIds = Arrays.stream(HomecareRoutine.values())
			.map(HomecareRoutine::getId)
			.toArray(Integer[]::new);
		assertThat(routines)
			.extracting(HomecareRoutineResponseDto::id)
			.containsExactly(expectedIds);
	}

	@Test
	@DisplayName("모든 홈케어 루틴 조회 - DTO 매핑 정확성 검증")
	void getAllHomecareRoutinesDtoMapping() {
		// when
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();

		// then
		HomecareRoutine firstEnum = HomecareRoutine.values()[0];
		assertThat(routines.getFirst())
			.satisfies(routine -> {
				assertThat(routine.id()).isEqualTo(firstEnum.getId());
				assertThat(routine.name()).isEqualTo(firstEnum.name());
				assertThat(routine.description()).isEqualTo(firstEnum.getDescription());
			});
	}
}
