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
	@DisplayName("모든 홈케어 루틴 조회 성공")
	void getAllHomecareRoutinesSuccess() {
		// when
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();

		// then
		assertThat(routines).isNotNull();
		assertThat(routines).hasSize(6);
		assertThat(routines).allSatisfy(routine -> {
			assertThat(routine.id()).isBetween(1, 6);
			assertThat(routine.name()).isNotBlank();
			assertThat(routine.description()).isNotBlank();
		});
	}

	@Test
	@DisplayName("홈케어 루틴 ID 순서 검증")
	void routineIdOrderValidation() {
		// when
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();

		// then
		assertThat(routines)
			.extracting(HomecareRoutineResponseDto::id)
			.containsExactly(1, 2, 3, 4, 5, 6);
	}

	@Test
	@DisplayName("첫 번째 루틴 DTO 변환 검증")
	void firstRoutineDtoMappingValidation() {
		// when
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();
		HomecareRoutineResponseDto firstRoutine = routines.get(0);

		// then
		assertThat(firstRoutine.id()).isEqualTo(1);
		assertThat(firstRoutine.name()).isEqualTo("SKIN_MOISTURIZING");
		assertThat(firstRoutine.description()).isEqualTo("피부 보습 관리");
	}
}
