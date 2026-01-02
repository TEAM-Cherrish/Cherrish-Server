package com.sopt.cherrish.domain.calendar.application.service;

import com.sopt.cherrish.domain.calendar.domain.model.UserProcedure;
import com.sopt.cherrish.domain.calendar.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.calendar.domain.service.CalendarValidator;
import com.sopt.cherrish.domain.calendar.domain.service.DowntimeCalculator;
import com.sopt.cherrish.domain.calendar.fixture.CalendarFixture;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDateDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarResponseDto;
import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarService 단위 테스트")
class CalendarServiceTest {

	@InjectMocks
	private CalendarService calendarService;

	@Mock
	private UserProcedureRepository userProcedureRepository;

	@Mock
	private DowntimeCalculator downtimeCalculator;

	@Mock
	private CalendarValidator calendarValidator;

	@Test
	@DisplayName("캘린더 조회 성공 - 시술이 1개 있는 경우")
	void getCalendar_Success_WithOneProcedure() {
		// given
		int year = 2025;
		int month = 1;
		Long userId = 1L;

		com.sopt.cherrish.domain.user.domain.model.User user = CalendarFixture.createUser(userId, "테스트", 25);
		Procedure procedure = CalendarFixture.createDefaultProcedure();
		UserProcedure userProcedure = CalendarFixture.createUserProcedure(
				1L,
				user,
				procedure,
				LocalDateTime.of(2025, 1, 15, 14, 0),
				7
		);

		given(userProcedureRepository.findByUserIdAndScheduledAtBetween(
				eq(userId),
				any(LocalDateTime.class),
				any(LocalDateTime.class)
		)).willReturn(List.of(userProcedure));

		given(downtimeCalculator.calculate(any(Integer.class), any())).willReturn(
				CalendarFixture.createDefaultDowntimePeriods()
		);

		// when
		CalendarResponseDto result = calendarService.getCalendar(userId, year, month);

		// then
		assertThat(result).isNotNull();
		assertThat(result.year()).isEqualTo(2025);
		assertThat(result.month()).isEqualTo(1);
		assertThat(result.dates()).hasSize(1);

		CalendarDateDto dateDto = result.dates().get(0);
		assertThat(dateDto.eventCount()).isEqualTo(1);
		assertThat(dateDto.events()).hasSize(1);
		assertThat(dateDto.events().get(0).name()).isEqualTo("레이저 토닝");
	}

	@Test
	@DisplayName("캘린더 조회 성공 - 시술이 없는 경우")
	void getCalendar_Success_WithNoProcedure() {
		// given
		int year = 2025;
		int month = 1;
		Long userId = 1L;

		given(userProcedureRepository.findByUserIdAndScheduledAtBetween(
				eq(userId),
				any(LocalDateTime.class),
				any(LocalDateTime.class)
		)).willReturn(Collections.emptyList());

		// when
		CalendarResponseDto result = calendarService.getCalendar(userId, year, month);

		// then
		assertThat(result).isNotNull();
		assertThat(result.year()).isEqualTo(2025);
		assertThat(result.month()).isEqualTo(1);
		assertThat(result.dates()).isEmpty();
	}

	@Test
	@DisplayName("캘린더 조회 성공 - 같은 날 여러 시술이 있는 경우")
	void getCalendar_Success_WithMultipleProceduresOnSameDay() {
		// given
		int year = 2025;
		int month = 1;
		Long userId = 1L;

		com.sopt.cherrish.domain.user.domain.model.User user = CalendarFixture.createUser(userId, "테스트", 25);
		Procedure procedure1 = CalendarFixture.createProcedure("레이저 토닝", "레이저", 3, 7);
		Procedure procedure2 = CalendarFixture.createProcedure("보톡스", "주사", 1, 3);

		LocalDateTime sameDateTime = LocalDateTime.of(2025, 1, 15, 14, 0);

		UserProcedure userProcedure1 = CalendarFixture.createUserProcedure(
				1L, user, procedure1, sameDateTime, 7
		);
		UserProcedure userProcedure2 = CalendarFixture.createUserProcedure(
				2L, user, procedure2, sameDateTime, 3
		);

		given(userProcedureRepository.findByUserIdAndScheduledAtBetween(
				eq(userId),
				any(LocalDateTime.class),
				any(LocalDateTime.class)
		)).willReturn(List.of(userProcedure1, userProcedure2));

		given(downtimeCalculator.calculate(any(Integer.class), any())).willReturn(
				CalendarFixture.createDefaultDowntimePeriods()
		);

		// when
		CalendarResponseDto result = calendarService.getCalendar(userId, year, month);

		// then
		assertThat(result).isNotNull();
		assertThat(result.dates()).hasSize(1);

		CalendarDateDto dateDto = result.dates().get(0);
		assertThat(dateDto.eventCount()).isEqualTo(2);
		assertThat(dateDto.events()).hasSize(2);
		assertThat(dateDto.events())
				.extracting("name")
				.containsExactlyInAnyOrder("레이저 토닝", "보톡스");
	}

	@Test
	@DisplayName("캘린더 조회 성공 - 다른 날짜에 여러 시술")
	void getCalendar_Success_WithMultipleProceduresOnDifferentDays() {
		// given
		int year = 2025;
		int month = 1;
		Long userId = 1L;

		com.sopt.cherrish.domain.user.domain.model.User user = CalendarFixture.createUser(userId, "테스트", 25);
		Procedure procedure1 = CalendarFixture.createDefaultProcedure();
		Procedure procedure2 = CalendarFixture.createProcedure("보톡스", "주사", 1, 3);

		UserProcedure userProcedure1 = CalendarFixture.createUserProcedure(
				1L, user, procedure1, LocalDateTime.of(2025, 1, 15, 14, 0), 7
		);
		UserProcedure userProcedure2 = CalendarFixture.createUserProcedure(
				2L, user, procedure2, LocalDateTime.of(2025, 1, 20, 16, 0), 3
		);

		given(userProcedureRepository.findByUserIdAndScheduledAtBetween(
				eq(userId),
				any(LocalDateTime.class),
				any(LocalDateTime.class)
		)).willReturn(List.of(userProcedure1, userProcedure2));

		given(downtimeCalculator.calculate(any(Integer.class), any())).willReturn(
				CalendarFixture.createDefaultDowntimePeriods()
		);

		// when
		CalendarResponseDto result = calendarService.getCalendar(userId, year, month);

		// then
		assertThat(result).isNotNull();
		assertThat(result.dates()).hasSize(2);

		// 날짜 순 정렬 확인
		assertThat(result.dates().get(0).date().getDayOfMonth()).isEqualTo(15);
		assertThat(result.dates().get(1).date().getDayOfMonth()).isEqualTo(20);
	}

	@Test
	@DisplayName("다운타임 일수 - 개인 설정이 있는 경우 우선 사용")
	void getCalendar_UseCustomDowntimeDays() {
		// given
		int year = 2025;
		int month = 1;
		Long userId = 1L;

		com.sopt.cherrish.domain.user.domain.model.User user = CalendarFixture.createUser(userId, "테스트", 25);
		Procedure procedure = CalendarFixture.createProcedure("레이저 토닝", "레이저", 3, 10);

		// 개인 설정: 5일 (시술 마스터는 최대 10일)
		UserProcedure userProcedure = CalendarFixture.createUserProcedure(
				1L, user, procedure, LocalDateTime.of(2025, 1, 15, 14, 0), 5
		);

		given(userProcedureRepository.findByUserIdAndScheduledAtBetween(
				eq(userId),
				any(LocalDateTime.class),
				any(LocalDateTime.class)
		)).willReturn(List.of(userProcedure));

		given(downtimeCalculator.calculate(any(Integer.class), any())).willReturn(
				CalendarFixture.createDefaultDowntimePeriods()
		);

		// when
		CalendarResponseDto result = calendarService.getCalendar(userId, year, month);

		// then
		assertThat(result.dates().get(0).events().get(0).downtimeDays()).isEqualTo(5);
	}

	@Test
	@DisplayName("다운타임 일수 - 개인 설정이 없으면 시술 마스터 최대값 사용")
	void getCalendar_UseMaxDowntimeDaysWhenCustomIsNull() {
		// given
		int year = 2025;
		int month = 1;
		Long userId = 1L;

		com.sopt.cherrish.domain.user.domain.model.User user = CalendarFixture.createUser(userId, "테스트", 25);
		Procedure procedure = CalendarFixture.createProcedure("레이저 토닝", "레이저", 3, 10);

		// 개인 설정 없음 (null)
		UserProcedure userProcedure = CalendarFixture.createUserProcedureWithoutCustomDowntime(
				1L, user, procedure, LocalDateTime.of(2025, 1, 15, 14, 0)
		);

		given(userProcedureRepository.findByUserIdAndScheduledAtBetween(
				eq(userId),
				any(LocalDateTime.class),
				any(LocalDateTime.class)
		)).willReturn(List.of(userProcedure));

		given(downtimeCalculator.calculate(any(Integer.class), any())).willReturn(
				CalendarFixture.createDefaultDowntimePeriods()
		);

		// when
		CalendarResponseDto result = calendarService.getCalendar(userId, year, month);

		// then
		assertThat(result.dates().get(0).events().get(0).downtimeDays()).isEqualTo(10);
	}
}
