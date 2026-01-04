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
		assertThat(routines).hasSize(6);
		assertThat(routines)
			.extracting(HomecareRoutineResponseDto::id)
			.containsExactly(1, 2, 3, 4, 5, 6);
		assertThat(routines)
			.extracting(HomecareRoutineResponseDto::name)
			.containsExactly(
				"SKIN_MOISTURIZING",
				"SKIN_BRIGHTENING",
				"WRINKLE_CARE",
				"TROUBLE_CARE",
				"PORE_CARE",
				"ELASTICITY_CARE"
			);
		assertThat(routines)
			.extracting(HomecareRoutineResponseDto::description)
			.containsExactly(
				"피부 보습 관리",
				"피부 미백 관리",
				"주름 개선 관리",
				"트러블 케어",
				"모공 관리",
				"탄력 관리"
			);
	}

	@Test
	@DisplayName("첫 번째 홈케어 루틴 검증")
	void getFirstRoutineValidation() {
		// when
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();
		HomecareRoutineResponseDto firstRoutine = routines.get(0);

		// then
		assertThat(firstRoutine.id()).isEqualTo(1);
		assertThat(firstRoutine.name()).isEqualTo("SKIN_MOISTURIZING");
		assertThat(firstRoutine.description()).isEqualTo("피부 보습 관리");
	}
}
