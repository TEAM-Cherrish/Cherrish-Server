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
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestItemDto;

@DisplayName("UserProcedureCreateRequestDto 단위 테스트")
class UserProcedureCreateRequestDtoTest {

	@Test
	@DisplayName("toEntities - User와 Procedure 리스트를 UserProcedure 엔티티 리스트로 변환")
	void toEntitiesSuccess() {
		// given
		User user = UserFixture.createUser();
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 15, 14, 30);

		Procedure procedure1 = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);
		Procedure procedure2 = ProcedureFixture.createProcedure("필러", "주사", 1, 3);
		List<Procedure> procedures = List.of(procedure1, procedure2);

		UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
			scheduledAt,
			List.of(
				new UserProcedureCreateRequestItemDto(procedure1.getId(), 5),
				new UserProcedureCreateRequestItemDto(procedure2.getId(), 7)
			)
		);

		// when
		List<UserProcedure> result = request.toEntities(user, procedures);

		// then
		assertThat(result).hasSize(2);

		// 첫 번째 UserProcedure 검증
		assertThat(result.get(0).getUser()).isEqualTo(user);
		assertThat(result.get(0).getProcedure()).isEqualTo(procedure1);
		assertThat(result.get(0).getScheduledAt()).isEqualTo(scheduledAt);
		assertThat(result.get(0).getDowntimeDays()).isEqualTo(5);

		// 두 번째 UserProcedure 검증
		assertThat(result.get(1).getUser()).isEqualTo(user);
		assertThat(result.get(1).getProcedure()).isEqualTo(procedure2);
		assertThat(result.get(1).getScheduledAt()).isEqualTo(scheduledAt);
		assertThat(result.get(1).getDowntimeDays()).isEqualTo(7);
	}
}
