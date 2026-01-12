package com.sopt.cherrish.domain.userprocedure.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.user.fixture.UserFixture;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.userprocedure.fixture.UserProcedureFixture;
import com.sopt.cherrish.domain.userprocedure.domain.model.ProcedurePhase;
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
		User user = UserFixture.createUser();
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
		UserProcedure saved1 = UserProcedureFixture.createUserProcedure(10L, user, procedure1, scheduledAt, 6);
		UserProcedure saved2 = UserProcedureFixture.createUserProcedure(11L, user, procedure2, scheduledAt, 3);

		given(userRepository.findById(userId)).willReturn(java.util.Optional.of(user));
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

		given(userRepository.findById(userId)).willReturn(java.util.Optional.empty());

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
		User user = UserFixture.createUser();
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 1, 16, 0);
		Procedure procedure = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);

		UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
			scheduledAt,
			List.of(
				new UserProcedureCreateRequestItemDto(procedure.getId(), 6),
				new UserProcedureCreateRequestItemDto(999L, 3)
			)
		);

		given(userRepository.findById(userId)).willReturn(java.util.Optional.of(user));
		given(procedureRepository.findAllById(any())).willReturn(List.of(procedure));

		// when & then
		assertThatThrownBy(() -> userProcedureService.createUserProcedures(userId, request))
			.isInstanceOf(ProcedureException.class)
			.hasFieldOrPropertyWithValue("errorCode", ProcedureErrorCode.PROCEDURE_NOT_FOUND);
	}

	@Test
	@DisplayName("경계값 테스트 - downtimeDays가 0인 경우 정상 등록")
	void createUserProceduresWithZeroDowntimeDays() {
		// given
		Long userId = 1L;
		User user = UserFixture.createUser();
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 1, 16, 0);
		Procedure procedure = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);

		UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
			scheduledAt,
			List.of(new UserProcedureCreateRequestItemDto(procedure.getId(), 0))
		);

		List<Procedure> procedures = List.of(procedure);
		UserProcedure saved = UserProcedureFixture.createUserProcedure(10L, user, procedure, scheduledAt, 0);

		given(userRepository.findById(userId)).willReturn(java.util.Optional.of(user));
		given(procedureRepository.findAllById(any())).willReturn(procedures);
		given(userProcedureRepository.saveAll(any())).willReturn(List.of(saved));

		// when
		UserProcedureCreateResponseDto result = userProcedureService.createUserProcedures(userId, request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getProcedures()).hasSize(1);
		assertThat(result.getProcedures().get(0).getDowntimeDays()).isEqualTo(0);
		verify(userProcedureRepository).saveAll(any());
	}

	@Test
	@DisplayName("단일 요청 - 동일한 scheduledAt에 여러 시술 등록")
	void createMultipleProceduresWithSameScheduledAt() {
		// given
		Long userId = 1L;
		User user = UserFixture.createUser();
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 1, 16, 0);
		Procedure procedure1 = ProcedureFixture.createProcedure("레이저 토닝", "레이저", 0, 1);
		Procedure procedure2 = ProcedureFixture.createProcedure("필러", "주사", 1, 3);
		Procedure procedure3 = ProcedureFixture.createProcedure("보톡스", "주사", 2, 4);

		UserProcedureCreateRequestDto request = new UserProcedureCreateRequestDto(
			scheduledAt,
			List.of(
				new UserProcedureCreateRequestItemDto(procedure1.getId(), 5),
				new UserProcedureCreateRequestItemDto(procedure2.getId(), 7),
				new UserProcedureCreateRequestItemDto(procedure3.getId(), 10)
			)
		);

		List<Procedure> procedures = List.of(procedure1, procedure2, procedure3);
		UserProcedure saved1 = UserProcedureFixture.createUserProcedure(10L, user, procedure1, scheduledAt, 5);
		UserProcedure saved2 = UserProcedureFixture.createUserProcedure(11L, user, procedure2, scheduledAt, 7);
		UserProcedure saved3 = UserProcedureFixture.createUserProcedure(12L, user, procedure3, scheduledAt, 10);

		given(userRepository.findById(userId)).willReturn(java.util.Optional.of(user));
		given(procedureRepository.findAllById(any())).willReturn(procedures);
		given(userProcedureRepository.saveAll(any())).willReturn(List.of(saved1, saved2, saved3));

		// when
		UserProcedureCreateResponseDto result = userProcedureService.createUserProcedures(userId, request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getProcedures()).hasSize(3);
		// 모든 시술이 동일한 scheduledAt을 가져야 함
		assertThat(result.getProcedures())
			.extracting("scheduledAt")
			.containsOnly(scheduledAt);
		// 각 시술의 다운타임은 개별적으로 설정됨
		assertThat(result.getProcedures())
			.extracting("downtimeDays")
			.containsExactlyInAnyOrder(5, 7, 10);
		verify(userProcedureRepository).saveAll(any());
	}

	@Test
	@DisplayName("다운타임 진행 중인 시술 조회 - COMPLETED 제외 및 단계/시간 순 정렬")
	void findRecentProceduresFiltersAndSorts() {
		// given
		Long userId = 1L;
		LocalDate today = LocalDate.of(2026, 1, 15);
		LocalDate fromDate = today.minusDays(30);
		User user = UserFixture.createUser();

		LocalDateTime baseDate = LocalDateTime.of(2026, 1, 13, 0, 0);
		UserProcedure sensitive = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("민감", "레이저", 0, 10),
			baseDate.plusHours(10),
			7
		);
		UserProcedure cautionLate = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("주의-늦게", "레이저", 0, 10),
			baseDate.plusHours(18),
			6
		);
		UserProcedure cautionEarly = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("주의-이르게", "레이저", 0, 10),
			baseDate.plusHours(9),
			5
		);
		UserProcedure recovery = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("회복", "레이저", 0, 10),
			baseDate.plusHours(12),
			3
		);
		UserProcedure completed = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("완료", "레이저", 0, 10),
			baseDate.plusHours(8),
			2
		);

		given(userProcedureRepository.findAllPastProcedures(userId, fromDate, today))
			.willReturn(List.of(cautionEarly, completed, recovery, sensitive, cautionLate));

		// when
		List<UserProcedure> result = userProcedureService.findRecentProcedures(userId, today);

		// then
		assertThat(result).hasSize(4);
		assertThat(result.get(0).calculateCurrentPhase(today)).isEqualTo(ProcedurePhase.SENSITIVE);
		assertThat(result.get(1).getProcedure().getName()).isEqualTo("주의-늦게");
		assertThat(result.get(2).getProcedure().getName()).isEqualTo("주의-이르게");
		assertThat(result.get(3).calculateCurrentPhase(today)).isEqualTo(ProcedurePhase.RECOVERY);
	}

	@Test
	@DisplayName("여러 날짜에 걸친 다운타임 진행 중인 시술 조회")
	void findRecentProceduresFromMultipleDates() {
		// given
		Long userId = 1L;
		LocalDate today = LocalDate.of(2026, 1, 15);
		LocalDate fromDate = today.minusDays(30);
		User user = UserFixture.createUser();

		// 1/10 시술 (다운타임 7일) - 1/15 기준 RECOVERY
		// sensitive: 1/10-1/12 (3일), caution: 1/13-1/14 (2일), recovery: 1/15-1/16 (2일)
		UserProcedure jan10 = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("필러", "주사", 0, 10),
			LocalDateTime.of(2026, 1, 10, 9, 0),
			7
		);
		// 1/12 시술 (다운타임 5일) - 1/15 기준 CAUTION
		// sensitive: 1/12-1/13 (2일), caution: 1/14-1/15 (2일), recovery: 1/16 (1일)
		UserProcedure jan12 = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("보톡스", "주사", 0, 10),
			LocalDateTime.of(2026, 1, 12, 10, 0),
			5
		);
		// 1/14 시술 (다운타임 4일) - 1/15 기준 SENSITIVE
		// sensitive: 1/14-1/15 (2일), caution: 1/16 (1일), recovery: 1/17 (1일)
		UserProcedure jan14 = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("레이저", "레이저", 0, 10),
			LocalDateTime.of(2026, 1, 14, 14, 0),
			4
		);
		// 1/8 시술 (다운타임 5일) - 1/15 기준 COMPLETED (제외되어야 함)
		// sensitive: 1/8-1/9 (2일), caution: 1/10-1/11 (2일), recovery: 1/12 (1일), 1/15 = COMPLETED
		UserProcedure jan8 = UserProcedureFixture.createUserProcedure(
			user,
			ProcedureFixture.createProcedure("리프팅", "시술", 0, 10),
			LocalDateTime.of(2026, 1, 8, 9, 0),
			5
		);

		given(userProcedureRepository.findAllPastProcedures(userId, fromDate, today))
			.willReturn(List.of(jan14, jan12, jan10, jan8));

		// when
		List<UserProcedure> result = userProcedureService.findRecentProcedures(userId, today);

		// then
		assertThat(result).hasSize(3);
		// SENSITIVE 먼저 - 레이저 (1/14 시술)
		assertThat(result.get(0).getProcedure().getName()).isEqualTo("레이저");
		assertThat(result.get(0).calculateCurrentPhase(today)).isEqualTo(ProcedurePhase.SENSITIVE);
		// CAUTION 두 번째 - 보톡스 (1/12 시술)
		assertThat(result.get(1).getProcedure().getName()).isEqualTo("보톡스");
		assertThat(result.get(1).calculateCurrentPhase(today)).isEqualTo(ProcedurePhase.CAUTION);
		// RECOVERY 마지막 - 필러 (1/10 시술)
		assertThat(result.get(2).getProcedure().getName()).isEqualTo("필러");
		assertThat(result.get(2).calculateCurrentPhase(today)).isEqualTo(ProcedurePhase.RECOVERY);
		// COMPLETED인 리프팅은 제외됨
	}

	@Test
	@DisplayName("다가오는 시술 조회 - 날짜별 그룹핑 및 최대 3개 날짜 제한")
	void findUpcomingProceduresGroupedByDateLimits() {
		// given
		Long userId = 1L;
		LocalDate today = LocalDate.of(2026, 1, 15);
		User user = UserFixture.createUser();

		List<UserProcedure> allUpcoming = List.of(
			UserProcedureFixture.createUserProcedure(
				user,
				ProcedureFixture.createProcedure("나흘뒤", "레이저", 0, 10),
				LocalDateTime.of(2026, 1, 19, 10, 0),
				3
			),
			UserProcedureFixture.createUserProcedure(
				user,
				ProcedureFixture.createProcedure("내일", "레이저", 0, 10),
				LocalDateTime.of(2026, 1, 16, 9, 0),
				1
			),
			UserProcedureFixture.createUserProcedure(
				user,
				ProcedureFixture.createProcedure("모레", "레이저", 0, 10),
				LocalDateTime.of(2026, 1, 17, 9, 0),
				2
			),
			UserProcedureFixture.createUserProcedure(
				user,
				ProcedureFixture.createProcedure("사흘뒤", "레이저", 0, 10),
				LocalDateTime.of(2026, 1, 18, 9, 0),
				4
			)
		);

		given(userProcedureRepository.findUpcomingProceduresGroupedByDate(userId, today.plusDays(1)))
			.willReturn(allUpcoming);

		// when
		Map<LocalDate, List<UserProcedure>> result =
			userProcedureService.findUpcomingProceduresGroupedByDate(userId, today, 3);

		// then
		assertThat(result).hasSize(3);
		assertThat(result.keySet())
			.containsExactly(
				LocalDate.of(2026, 1, 16),
				LocalDate.of(2026, 1, 17),
				LocalDate.of(2026, 1, 18)
			);
	}
}
