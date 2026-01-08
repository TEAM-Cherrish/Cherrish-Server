package com.sopt.cherrish.domain.userprocedure.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureRepository;
import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.procedure.exception.ProcedureException;
import com.sopt.cherrish.domain.procedure.fixture.ProcedureFixture;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureFixture;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestItemDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureCreateResponseDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProcedureService 단위 테스트")
class UserProcedureServiceTest {

	@InjectMocks
	private UserProcedureService userProcedureService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ProcedureRepository procedureRepository;

	@Mock
	private UserProcedureRepository userProcedureRepository;

	@Test
	@DisplayName("사용자 시술 일정 등록 성공")
	void createUserProceduresSuccess() {
		// given
		Long userId = 1L;
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 1, 16, 0);
		Procedure procedure1 = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);
		Procedure procedure2 = ProcedureFixture.createProcedure("필러", "주사", 1, 3);

		UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
			scheduledAt,
			List.of(
				new UserProcedureCreateRequestItemDto(procedure1.getId(), 6),
				new UserProcedureCreateRequestItemDto(procedure2.getId(), 3)
			)
		);

		List<Procedure> procedures = List.of(procedure1, procedure2);
		UserProcedure saved1 = UserProcedureFixture.createUserProcedure(10L, userId, procedure1, scheduledAt, 6);
		UserProcedure saved2 = UserProcedureFixture.createUserProcedure(11L, userId, procedure2, scheduledAt, 3);

		given(userRepository.existsById(userId)).willReturn(true);
		given(procedureRepository.findAllById(any())).willReturn(procedures);
		given(userProcedureRepository.saveAll(any())).willReturn(List.of(saved1, saved2));

		// when
		UserProcedureCreateResponseDto result = userProcedureService.createUserProcedures(userId, request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getProcedures()).hasSize(2);
		assertThat(result.getProcedures().get(0).getUserProcedureId()).isEqualTo(10L);
		assertThat(result.getProcedures().get(0).getProcedureId()).isEqualTo(procedure1.getId());
		assertThat(result.getProcedures().get(0).getProcedureName()).isEqualTo("레이저 토닝");
		assertThat(result.getProcedures().get(0).getScheduledAt()).isEqualTo(scheduledAt);
		assertThat(result.getProcedures().get(0).getDowntimeDays()).isEqualTo(6);
		verify(userProcedureRepository).saveAll(any());
	}

	@Test
	@DisplayName("사용자 시술 일정 등록 실패 - 존재하지 않는 사용자")
	void createUserProceduresFailUserNotFound() {
		// given
		Long userId = 999L;
		UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
			LocalDateTime.of(2025, 1, 1, 16, 0),
			List.of(new UserProcedureCreateRequestItemDto(1L, 6))
		);

		given(userRepository.existsById(userId)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> userProcedureService.createUserProcedures(userId, request))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("사용자 시술 일정 등록 실패 - 존재하지 않는 시술")
	void createUserProceduresFailProcedureNotFound() {
		// given
		Long userId = 1L;
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 1, 16, 0);
		Procedure procedure = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);

		UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
			scheduledAt,
			List.of(
				new UserProcedureCreateRequestItemDto(procedure.getId(), 6),
				new UserProcedureCreateRequestItemDto(999L, 3)
			)
		);

		given(userRepository.existsById(userId)).willReturn(true);
		given(procedureRepository.findAllById(any())).willReturn(List.of(procedure));

		// when & then
		assertThatThrownBy(() -> userProcedureService.createUserProcedures(userId, request))
			.isInstanceOf(ProcedureException.class)
			.hasFieldOrPropertyWithValue("errorCode", ProcedureErrorCode.PROCEDURE_NOT_FOUND);
	}
}
