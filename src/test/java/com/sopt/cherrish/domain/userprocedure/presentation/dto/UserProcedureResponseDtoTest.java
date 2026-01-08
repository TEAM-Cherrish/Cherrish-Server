package com.sopt.cherrish.domain.userprocedure.presentation.dto;

import static org.assertj.core.api.Assertions.assertThat;

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

		UserProcedure userProcedure = UserProcedureFixture.createUserProcedure(
			10L,
			user,
			procedure,
			scheduledAt,
			5
		);

		// when
		UserProcedureResponseDto result = UserProcedureResponseDto.from(userProcedure);

		// then
		assertThat(result.getUserProcedureId()).isEqualTo(10L);
		assertThat(result.getProcedureId()).isEqualTo(procedure.getId());
		assertThat(result.getProcedureName()).isEqualTo("레이저 토닝");
		assertThat(result.getScheduledAt()).isEqualTo(scheduledAt);
		assertThat(result.getDowntimeDays()).isEqualTo(5);
	}

	@Test
	@DisplayName("from - UserProcedure 리스트를 CreateResponseDto로 변환")
	void fromListToDto() {
		// given
		User user = UserFixture.createUser();
		Procedure procedure1 = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);
		Procedure procedure2 = ProcedureFixture.createProcedure("필러", "주사", 1, 3);
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 15, 14, 30);

		List<UserProcedure> userProcedures = List.of(
			UserProcedureFixture.createUserProcedure(10L, user, procedure1, scheduledAt, 5),
			UserProcedureFixture.createUserProcedure(11L, user, procedure2, scheduledAt, 7)
		);

		// when
		UserProcedureCreateResponseDto result = UserProcedureCreateResponseDto.from(userProcedures);

		// then
		assertThat(result.getProcedures()).hasSize(2);
		assertThat(result.getProcedures().get(0).getUserProcedureId()).isEqualTo(10L);
		assertThat(result.getProcedures().get(0).getProcedureName()).isEqualTo("레이저 토닝");
		assertThat(result.getProcedures().get(1).getUserProcedureId()).isEqualTo(11L);
		assertThat(result.getProcedures().get(1).getProcedureName()).isEqualTo("필러");
	}
}
