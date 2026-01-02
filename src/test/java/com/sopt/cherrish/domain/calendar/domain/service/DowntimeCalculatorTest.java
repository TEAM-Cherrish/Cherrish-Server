package com.sopt.cherrish.domain.calendar.domain.service;

import com.sopt.cherrish.domain.calendar.domain.vo.DowntimePeriods;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DowntimeCalculator 단위 테스트")
class DowntimeCalculatorTest {

	private final DowntimeCalculator downtimeCalculator = new DowntimeCalculator();

	@Test
	@DisplayName("다운타임 7일 - 3등분 (민감 3일, 주의 2일, 회복 2일)")
	void calculate_SevenDays() {
		// given
		int downtimeDays = 7;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, startDate);

		// then
		assertThat(periods.sensitiveDays()).hasSize(3);
		assertThat(periods.cautionDays()).hasSize(2);
		assertThat(periods.recoveryDays()).hasSize(2);

		// 날짜 연속성 검증
		assertThat(periods.sensitiveDays()).containsExactly(
				LocalDate.of(2025, 1, 15),
				LocalDate.of(2025, 1, 16),
				LocalDate.of(2025, 1, 17)
		);
		assertThat(periods.cautionDays()).containsExactly(
				LocalDate.of(2025, 1, 18),
				LocalDate.of(2025, 1, 19)
		);
		assertThat(periods.recoveryDays()).containsExactly(
				LocalDate.of(2025, 1, 20),
				LocalDate.of(2025, 1, 21)
		);
	}

	@Test
	@DisplayName("다운타임 3일 - 3등분 (민감 1일, 주의 1일, 회복 1일)")
	void calculate_ThreeDays() {
		// given
		int downtimeDays = 3;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, startDate);

		// then
		assertThat(periods.sensitiveDays()).hasSize(1);
		assertThat(periods.cautionDays()).hasSize(1);
		assertThat(periods.recoveryDays()).hasSize(1);
	}

	@Test
	@DisplayName("다운타임 10일 - 나머지 2일 분배 (민감 4일, 주의 3일, 회복 3일)")
	void calculate_TenDays() {
		// given
		int downtimeDays = 10;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, startDate);

		// then
		// 10 / 3 = 3 나머지 1
		// 민감: 3 + 1 = 4일
		// 주의: 3일
		// 회복: 3일
		assertThat(periods.sensitiveDays()).hasSize(4);
		assertThat(periods.cautionDays()).hasSize(3);
		assertThat(periods.recoveryDays()).hasSize(3);
	}

	@Test
	@DisplayName("다운타임 8일 - 나머지 2일 분배 (민감 3일, 주의 3일, 회복 2일)")
	void calculate_EightDays() {
		// given
		int downtimeDays = 8;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, startDate);

		// then
		// 8 / 3 = 2 나머지 2
		// 민감: 2 + 1 = 3일
		// 주의: 2 + 1 = 3일
		// 회복: 2일
		assertThat(periods.sensitiveDays()).hasSize(3);
		assertThat(periods.cautionDays()).hasSize(3);
		assertThat(periods.recoveryDays()).hasSize(2);
	}

	@Test
	@DisplayName("다운타임 1일 - 모두 민감 기간")
	void calculate_OneDay() {
		// given
		int downtimeDays = 1;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, startDate);

		// then
		assertThat(periods.sensitiveDays()).hasSize(1);
		assertThat(periods.cautionDays()).isEmpty();
		assertThat(periods.recoveryDays()).isEmpty();
	}

	@Test
	@DisplayName("월을 넘어가는 다운타임 계산")
	void calculate_CrossMonth() {
		// given
		int downtimeDays = 7;
		LocalDate startDate = LocalDate.of(2025, 1, 28);

		// when
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, startDate);

		// then
		assertThat(periods.sensitiveDays()).hasSize(3);
		assertThat(periods.cautionDays()).hasSize(2);
		assertThat(periods.recoveryDays()).hasSize(2);

		// 1월 28일 ~ 2월 3일
		assertThat(periods.sensitiveDays()).containsExactly(
				LocalDate.of(2025, 1, 28),
				LocalDate.of(2025, 1, 29),
				LocalDate.of(2025, 1, 30)
		);
		assertThat(periods.cautionDays()).containsExactly(
				LocalDate.of(2025, 1, 31),
				LocalDate.of(2025, 2, 1)
		);
		assertThat(periods.recoveryDays()).containsExactly(
				LocalDate.of(2025, 2, 2),
				LocalDate.of(2025, 2, 3)
		);
	}
}
