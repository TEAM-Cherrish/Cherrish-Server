package com.sopt.cherrish.domain.challenge.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("HomecareRoutine 도메인 모델 테스트")
class HomecareRoutineTest {

	@ParameterizedTest
	@EnumSource(HomecareRoutine.class)
	@DisplayName("ID로 HomecareRoutine 조회 성공")
	void fromIdSuccess(HomecareRoutine expectedRoutine) {
		// when
		HomecareRoutine result = HomecareRoutine.fromId(expectedRoutine.getId());

		// then
		assertThat(result).isEqualTo(expectedRoutine);
		assertThat(result.getId()).isEqualTo(expectedRoutine.getId());
		assertThat(result.name()).isEqualTo(expectedRoutine.name());
		assertThat(result.getDescription()).isEqualTo(expectedRoutine.getDescription());
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
