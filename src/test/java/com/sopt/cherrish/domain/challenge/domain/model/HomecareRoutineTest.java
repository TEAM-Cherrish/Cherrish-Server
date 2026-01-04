package com.sopt.cherrish.domain.challenge.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("HomecareRoutine 도메인 모델 테스트")
class HomecareRoutineTest {

	@ParameterizedTest
	@CsvSource({
		"1, SKIN_MOISTURIZING, 피부 보습 관리",
		"2, SKIN_BRIGHTENING, 피부 미백 관리",
		"3, WRINKLE_CARE, 주름 개선 관리",
		"4, TROUBLE_CARE, 트러블 케어",
		"5, PORE_CARE, 모공 관리",
		"6, ELASTICITY_CARE, 탄력 관리"
	})
	@DisplayName("ID로 HomecareRoutine 조회 성공")
	void fromIdSuccess(int id, String expectedName, String expectedDescription) {
		// when
		HomecareRoutine routine = HomecareRoutine.fromId(id);

		// then
		assertThat(routine.getId()).isEqualTo(id);
		assertThat(routine.name()).isEqualTo(expectedName);
		assertThat(routine.getDescription()).isEqualTo(expectedDescription);
	}

	@ParameterizedTest
	@CsvSource({
		"0",
		"7",
		"-1",
		"100"
	})
	@DisplayName("ID로 HomecareRoutine 조회 실패 - 유효하지 않은 ID")
	void fromIdInvalidId(int invalidId) {
		// when & then
		assertThatThrownBy(() -> HomecareRoutine.fromId(invalidId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("존재하지 않는 홈케어 루틴 ID입니다");
	}

	@Test
	@DisplayName("모든 HomecareRoutine enum 값 확인")
	void allValuesTest() {
		// when
		HomecareRoutine[] routines = HomecareRoutine.values();

		// then
		assertThat(routines).hasSize(6);
		assertThat(routines)
			.extracting(HomecareRoutine::getId)
			.containsExactly(1, 2, 3, 4, 5, 6);
	}
}
