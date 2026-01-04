package com.sopt.cherrish.domain.challenge.domain.model;

import static org.assertj.core.api.Assertions.*;

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
	@DisplayName("HomecareRoutine ID 순차 증가 검증")
	void routineIdsAreSequential() {
		// when
		HomecareRoutine[] routines = HomecareRoutine.values();

		// then
		assertThat(routines)
			.hasSize(6)
			.extracting(HomecareRoutine::getId)
			.containsExactly(1, 2, 3, 4, 5, 6);
	}
}
