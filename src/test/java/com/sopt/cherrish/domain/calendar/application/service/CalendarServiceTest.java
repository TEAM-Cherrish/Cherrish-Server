package com.sopt.cherrish.domain.calendar.application.service;

import static com.sopt.cherrish.domain.calendar.fixture.CalendarTestFixture.createMockProcedure;
import static com.sopt.cherrish.domain.calendar.fixture.CalendarTestFixture.createMockUser;
import static com.sopt.cherrish.domain.calendar.fixture.CalendarTestFixture.createUserProcedure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDailyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarMonthlyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventDowntimeResponseDto;
import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.userprocedure.exception.UserProcedureErrorCode;
import com.sopt.cherrish.domain.userprocedure.exception.UserProcedureException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarService 단위 테스트")
class CalendarServiceTest {

	@InjectMocks
	private CalendarService calendarService;

	@Mock
	private UserProcedureRepository userProcedureRepository;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("월별 캘린더 조회 성공")
	void getMonthlyCalendarSuccess() {
		// given
		Long userId = 1L;
		int year = 2025;
		int month = 1;

		Map<Integer, Long> mockCounts = Map.of(
			1, 2L,
			5, 1L,
			10, 3L
		);

		given(userRepository.existsById(userId)).willReturn(true);
		given(userProcedureRepository.findMonthlyProcedureCounts(userId, year, month))
			.willReturn(mockCounts);

		// when
		CalendarMonthlyResponseDto result = calendarService.getMonthlyCalendar(userId, year, month);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getDailyProcedureCounts()).hasSize(3);
		assertThat(result.getDailyProcedureCounts().get(1)).isEqualTo(2L);
		assertThat(result.getDailyProcedureCounts().get(5)).isEqualTo(1L);
		assertThat(result.getDailyProcedureCounts().get(10)).isEqualTo(3L);
	}

	@Test
	@DisplayName("월별 캘린더 조회 실패 - 존재하지 않는 사용자")
	void getMonthlyCalendarUserNotFound() {
		// given
		Long userId = 999L;
		int year = 2025;
		int month = 1;

		given(userRepository.existsById(userId)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> calendarService.getMonthlyCalendar(userId, year, month))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("일자별 시술 상세 조회 성공")
	void getDailyCalendarSuccess() {
		// given
		Long userId = 1L;
		LocalDate date = LocalDate.of(2025, 1, 15);

		User mockUser = createMockUser("테스트 사용자", 25);
		Procedure mockProcedure1 = createMockProcedure("레이저 토닝");
		Procedure mockProcedure2 = createMockProcedure("필러");

		UserProcedure userProcedure1 = createUserProcedure(
			mockUser, mockProcedure1, LocalDateTime.of(2025, 1, 15, 14, 0), 9);
		UserProcedure userProcedure2 = createUserProcedure(
			mockUser, mockProcedure2, LocalDateTime.of(2025, 1, 15, 16, 0), 6);

		given(userRepository.existsById(userId)).willReturn(true);
		given(userProcedureRepository.findDailyProcedures(userId, date))
			.willReturn(List.of(userProcedure1, userProcedure2));

		// when
		CalendarDailyResponseDto result = calendarService.getDailyCalendar(userId, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getEventCount()).isEqualTo(2);
		assertThat(result.getEvents()).hasSize(2);

		// 첫 번째 시술 검증
		assertThat(result.getEvents().get(0).getName()).isEqualTo("레이저 토닝");
		assertThat(result.getEvents().get(0).getDowntimeDays()).isEqualTo(9);

		// 두 번째 시술 검증
		assertThat(result.getEvents().get(1).getName()).isEqualTo("필러");
		assertThat(result.getEvents().get(1).getDowntimeDays()).isEqualTo(6);
	}

	@Test
	@DisplayName("일자별 시술 상세 조회 성공 - 시술 없음")
	void getDailyCalendarSuccessWithNoProcedures() {
		// given
		Long userId = 1L;
		LocalDate date = LocalDate.of(2025, 1, 15);

		given(userRepository.existsById(userId)).willReturn(true);
		given(userProcedureRepository.findDailyProcedures(userId, date))
			.willReturn(List.of());

		// when
		CalendarDailyResponseDto result = calendarService.getDailyCalendar(userId, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getEventCount()).isZero();
		assertThat(result.getEvents()).isEmpty();
	}

	@ParameterizedTest
	@DisplayName("시술 다운타임 상세 조회 성공 - 다운타임 기간 계산 검증")
	@CsvSource({
		"0, 0, 0, 0",  // 다운타임 0일 -> 빈 리스트
		"7, 3, 2, 2",  // 7일 -> 7/3=2...1 -> 민감기 3, 주의기 2, 회복기 2
		"8, 3, 3, 2"   // 8일 -> 8/3=2...2 -> 민감기 3, 주의기 3, 회복기 2
	})
	void getEventDowntimeVariations(
		int downtimeDays,
		int expectedSensitive,
		int expectedCaution,
		int expectedRecovery
	) {
		// given
		Long userId = 1L;
		Long userProcedureId = 101L;

		User mockUser = createMockUser("테스트 사용자", 25);
		Procedure mockProcedure = createMockProcedure("레이저 토닝");
		UserProcedure userProcedure = createUserProcedure(
			mockUser, mockProcedure, LocalDateTime.of(2025, 1, 15, 14, 0), downtimeDays);

		given(userRepository.existsById(userId)).willReturn(true);
		given(userProcedureRepository.findByIdAndUserId(userProcedureId, userId))
			.willReturn(Optional.of(userProcedure));

		// when
		ProcedureEventDowntimeResponseDto result = calendarService.getEventDowntime(userId, userProcedureId);

		// then
		assertThat(result.getDowntimeDays()).isEqualTo(downtimeDays);
		assertThat(result.getSensitiveDays()).hasSize(expectedSensitive);
		assertThat(result.getCautionDays()).hasSize(expectedCaution);
		assertThat(result.getRecoveryDays()).hasSize(expectedRecovery);
	}

	@Test
	@DisplayName("일자별 시술 상세 조회 실패 - 존재하지 않는 사용자")
	void getDailyCalendarUserNotFound() {
		// given
		Long userId = 999L;
		LocalDate date = LocalDate.of(2025, 1, 15);

		given(userRepository.existsById(userId)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> calendarService.getDailyCalendar(userId, date))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("시술 다운타임 상세 조회 실패 - 존재하지 않는 시술 일정")
	void getEventDowntimeNotFound() {
		// given
		Long userId = 1L;
		Long userProcedureId = 404L;

		given(userRepository.existsById(userId)).willReturn(true);
		given(userProcedureRepository.findByIdAndUserId(userProcedureId, userId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> calendarService.getEventDowntime(userId, userProcedureId))
			.isInstanceOf(UserProcedureException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserProcedureErrorCode.USER_PROCEDURE_NOT_FOUND);
	}
}
