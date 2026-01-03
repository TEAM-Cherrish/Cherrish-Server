package com.sopt.cherrish.domain.procedure.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.procedure.exception.ProcedureException;
import com.sopt.cherrish.domain.procedure.fixture.ProcedureFixture;

@DisplayName("Procedure 엔티티 단위 테스트")
class ProcedureTest {

	@Test
	@DisplayName("Procedure 생성 성공")
	void createProcedureSuccess() {
		// given & when
		Procedure procedure = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 1, 5);

		// then
		assertThat(procedure).isNotNull();
		assertThat(procedure.getName()).isEqualTo("레이저 토닝");
		assertThat(procedure.getCategory()).isEqualTo("레이저");
		assertThat(procedure.getMinDowntimeDays()).isEqualTo(1);
		assertThat(procedure.getMaxDowntimeDays()).isEqualTo(5);
	}

	@Test
	@DisplayName("Procedure 생성 성공 - 다운타임이 같은 경우")
	void createProcedureWithSameDowntime() {
		// given & when
		Procedure procedure = ProcedureFixture.createProcedure("보톡스", "주사", 3, 3);

		// then
		assertThat(procedure).isNotNull();
		assertThat(procedure.getMinDowntimeDays()).isEqualTo(3);
		assertThat(procedure.getMaxDowntimeDays()).isEqualTo(3);
	}

	@Test
	@DisplayName("Procedure 생성 성공 - 다운타임이 0인 경우")
	void createProcedureWithZeroDowntime() {
		// given & when
		Procedure procedure = ProcedureFixture.createProcedure("아쿠아필", "관리", 0, 0);

		// then
		assertThat(procedure).isNotNull();
		assertThat(procedure.getMinDowntimeDays()).isZero();
		assertThat(procedure.getMaxDowntimeDays()).isZero();
	}

	@Test
	@DisplayName("Procedure 생성 실패 - 최소 다운타임이 음수")
	void createProcedureFailWithNegativeMinDowntime() {
		// when & then
		assertThatThrownBy(() -> Procedure.builder()
			.name("레이저 토닝")
			.category("레이저")
			.minDowntimeDays(-1)
			.maxDowntimeDays(5)
			.build())
			.isInstanceOf(ProcedureException.class)
			.hasFieldOrPropertyWithValue("errorCode", ProcedureErrorCode.INVALID_DOWNTIME_VALUE);
	}

	@Test
	@DisplayName("Procedure 생성 실패 - 최대 다운타임이 음수")
	void createProcedureFailWithNegativeMaxDowntime() {
		// when & then
		assertThatThrownBy(() -> Procedure.builder()
			.name("레이저 토닝")
			.category("레이저")
			.minDowntimeDays(1)
			.maxDowntimeDays(-1)
			.build())
			.isInstanceOf(ProcedureException.class)
			.hasFieldOrPropertyWithValue("errorCode", ProcedureErrorCode.INVALID_DOWNTIME_VALUE);
	}

	@Test
	@DisplayName("Procedure 생성 실패 - 최소 다운타임이 최대 다운타임보다 큼")
	void createProcedureFailWithInvalidDowntimeRange() {
		// when & then
		assertThatThrownBy(() -> Procedure.builder()
			.name("레이저 토닝")
			.category("레이저")
			.minDowntimeDays(10)
			.maxDowntimeDays(5)
			.build())
			.isInstanceOf(ProcedureException.class)
			.hasFieldOrPropertyWithValue("errorCode", ProcedureErrorCode.INVALID_DOWNTIME_RANGE);
	}
}
