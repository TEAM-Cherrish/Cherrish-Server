package com.sopt.cherrish.domain.calendar.domain.service;

import com.sopt.cherrish.domain.calendar.domain.vo.DowntimePeriods;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DowntimeCalculator 단위 테스트")
class DowntimeCalculatorTest {

	private final DowntimeCalculator downtimeCalculator = new DowntimeCalculator();

	@ParameterizedTest
	@CsvSource({
			"1,  1, 0, 0",  // 1일: 민감 1일
			"3,  1, 1, 1",  // 3일: 각 1일씩
			"7,  3, 2, 2",  // 7일: 민감 3일, 주의 2일, 회복 2일
			"8,  3, 3, 2",  // 8일: 민감 3일, 주의 3일, 회복 2일
			"10, 4, 3, 3"   // 10일: 민감 4일, 주의 3일, 회복 3일
	})
	@DisplayName("다운타임 일수별 기간 분배 계산")
	void calculate_VariousDays(
			int downtimeDays,
			int expectedSensitive,
			int expectedCaution,
			int expectedRecovery
	) {
		// given
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, startDate);

		// then
		assertThat(periods.sensitiveDays()).hasSize(expectedSensitive);
		assertThat(periods.cautionDays()).hasSize(expectedCaution);
		assertThat(periods.recoveryDays()).hasSize(expectedRecovery);
	}

	@Test
	@DisplayName("다운타임 7일 - 날짜 연속성 검증")
	void calculate_SevenDays_DateContinuity() {
		// given
		int downtimeDays = 7;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, startDate);

		// then
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
