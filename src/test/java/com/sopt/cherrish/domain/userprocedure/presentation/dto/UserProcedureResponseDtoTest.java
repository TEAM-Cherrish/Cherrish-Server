package com.sopt.cherrish.domain.userprocedure.presentation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.fixture.ProcedureFixture;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.fixture.UserFixture;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureFixture;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureCreateResponseDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureResponseDto;

@DisplayName("UserProcedureResponseDto 단위 테스트")
class UserProcedureResponseDtoTest {

	@Test
	@DisplayName("from - UserProcedure 엔티티를 ResponseDto로 변환")
	void fromEntityToDto() {
		// given
		User user = UserFixture.createUser();
		Procedure procedure = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 15, 14, 30);
		LocalDate recoveryTargetDate = LocalDate.of(2025, 1, 20);

		UserProcedure userProcedure = UserProcedureFixture.createUserProcedure(
			10L,
			user,
			procedure,
			scheduledAt,
			5,
			recoveryTargetDate
		);

		// when
		UserProcedureResponseDto result = UserProcedureResponseDto.from(userProcedure);

		// then
		assertThat(result.userProcedureId()).isEqualTo(10L);
		assertThat(result.procedureId()).isEqualTo(procedure.getId());
		assertThat(result.procedureName()).isEqualTo("레이저 토닝");
		assertThat(result.scheduledAt()).isEqualTo(scheduledAt);
		assertThat(result.downtimeDays()).isEqualTo(5);
		assertThat(result.recoveryTargetDate()).isEqualTo(recoveryTargetDate);
	}

	@Test
	@DisplayName("from - UserProcedure 리스트를 CreateResponseDto로 변환")
	void fromListToDto() {
		// given
		User user = UserFixture.createUser();
		Procedure procedure1 = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);
		Procedure procedure2 = ProcedureFixture.createProcedure("필러", "주사", 1, 3);
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 15, 14, 30);
		LocalDate recoveryTargetDate = LocalDate.of(2025, 1, 20);

		List<UserProcedure> userProcedures = List.of(
			UserProcedureFixture.createUserProcedure(10L, user, procedure1, scheduledAt, 5, recoveryTargetDate),
			UserProcedureFixture.createUserProcedure(11L, user, procedure2, scheduledAt, 7, recoveryTargetDate)
		);

		// when
		UserProcedureCreateResponseDto result = UserProcedureCreateResponseDto.from(userProcedures);

		// then
		assertThat(result.procedures()).hasSize(2);

		// 첫 번째 항목 검증
		assertThat(result.procedures().get(0).userProcedureId()).isEqualTo(10L);
		assertThat(result.procedures().get(0).procedureId()).isEqualTo(procedure1.getId());
		assertThat(result.procedures().get(0).procedureName()).isEqualTo("레이저 토닝");
		assertThat(result.procedures().get(0).scheduledAt()).isEqualTo(scheduledAt);
		assertThat(result.procedures().get(0).downtimeDays()).isEqualTo(5);
		assertThat(result.procedures().get(0).recoveryTargetDate()).isEqualTo(recoveryTargetDate);

		// 두 번째 항목 검증
		assertThat(result.procedures().get(1).userProcedureId()).isEqualTo(11L);
		assertThat(result.procedures().get(1).procedureId()).isEqualTo(procedure2.getId());
		assertThat(result.procedures().get(1).procedureName()).isEqualTo("필러");
		assertThat(result.procedures().get(1).scheduledAt()).isEqualTo(scheduledAt);
		assertThat(result.procedures().get(1).downtimeDays()).isEqualTo(7);
		assertThat(result.procedures().get(1).recoveryTargetDate()).isEqualTo(recoveryTargetDate);
	}
}
