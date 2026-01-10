package com.sopt.cherrish.domain.userprocedure.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.sopt.cherrish.domain.userprocedure.exception.UserProcedureErrorCode;
import com.sopt.cherrish.domain.userprocedure.exception.UserProcedureException;

@DisplayName("DowntimePeriod VO 단위 테스트")
class DowntimePeriodTest {

	@Test
	@DisplayName("다운타임 0일 - 빈 기간 반환")
	void calculateZeroDowntime() {
		// given
		int downtimeDays = 0;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriod result = DowntimePeriod.calculate(downtimeDays, startDate);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getSensitiveDays()).isEmpty();
		assertThat(result.getCautionDays()).isEmpty();
		assertThat(result.getRecoveryDays()).isEmpty();
		assertThat(result.isEmpty()).isTrue();
	}

	@ParameterizedTest
	@DisplayName("다운타임 3으로 나누어떨어지는 경우 - 균등 분배")
	@CsvSource({
		"3, 1, 1, 1",   // 3일 -> 1/1/1
		"6, 2, 2, 2",   // 6일 -> 2/2/2
		"9, 3, 3, 3",   // 9일 -> 3/3/3
		"30, 10, 10, 10" // 30일 (최댓값) -> 10/10/10
	})
	void calculateEvenlyDivisible(int downtimeDays, int expectedSensitive, int expectedCaution,
		int expectedRecovery) {
		// given
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriod result = DowntimePeriod.calculate(downtimeDays, startDate);

		// then
		assertThat(result.getSensitiveDays()).hasSize(expectedSensitive);
		assertThat(result.getCautionDays()).hasSize(expectedCaution);
		assertThat(result.getRecoveryDays()).hasSize(expectedRecovery);
		assertThat(result.isEmpty()).isFalse();
	}

	@ParameterizedTest
	@DisplayName("다운타임 나머지가 1인 경우 - 민감기에 1일 추가")
	@CsvSource({
		"1, 1, 0, 0",   // 1일 -> 1/0/0
		"4, 2, 1, 1",   // 4일 -> 2/1/1
		"7, 3, 2, 2",   // 7일 -> 3/2/2
		"10, 4, 3, 3"   // 10일 -> 4/3/3
	})
	void calculateRemainder1(int downtimeDays, int expectedSensitive, int expectedCaution, int expectedRecovery) {
		// given
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriod result = DowntimePeriod.calculate(downtimeDays, startDate);

		// then
		assertThat(result.getSensitiveDays()).hasSize(expectedSensitive);
		assertThat(result.getCautionDays()).hasSize(expectedCaution);
		assertThat(result.getRecoveryDays()).hasSize(expectedRecovery);
	}

	@ParameterizedTest
	@DisplayName("다운타임 나머지가 2인 경우 - 민감기/주의기에 각 1일 추가")
	@CsvSource({
		"2, 1, 1, 0",   // 2일 -> 1/1/0
		"5, 2, 2, 1",   // 5일 -> 2/2/1
		"8, 3, 3, 2",   // 8일 -> 3/3/2
		"11, 4, 4, 3"   // 11일 -> 4/4/3
	})
	void calculateRemainder2(int downtimeDays, int expectedSensitive, int expectedCaution, int expectedRecovery) {
		// given
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriod result = DowntimePeriod.calculate(downtimeDays, startDate);

		// then
		assertThat(result.getSensitiveDays()).hasSize(expectedSensitive);
		assertThat(result.getCautionDays()).hasSize(expectedCaution);
		assertThat(result.getRecoveryDays()).hasSize(expectedRecovery);
	}

	@Test
	@DisplayName("날짜 순서 검증 - 민감기 -> 주의기 -> 회복기 순서로 연속")
	void validateDateSequence() {
		// given
		int downtimeDays = 9;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when
		DowntimePeriod result = DowntimePeriod.calculate(downtimeDays, startDate);

		// then
		// 민감기: 1/15, 1/16, 1/17
		assertThat(result.getSensitiveDays()).containsExactly(
			LocalDate.of(2025, 1, 15),
			LocalDate.of(2025, 1, 16),
			LocalDate.of(2025, 1, 17)
		);

		// 주의기: 1/18, 1/19, 1/20
		assertThat(result.getCautionDays()).containsExactly(
			LocalDate.of(2025, 1, 18),
			LocalDate.of(2025, 1, 19),
			LocalDate.of(2025, 1, 20)
		);

		// 회복기: 1/21, 1/22, 1/23
		assertThat(result.getRecoveryDays()).containsExactly(
			LocalDate.of(2025, 1, 21),
			LocalDate.of(2025, 1, 22),
			LocalDate.of(2025, 1, 23)
		);
	}

	@Test
	@DisplayName("다운타임 음수 - 예외 발생")
	void calculateNegativeDowntime() {
		// given
		int downtimeDays = -1;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when & then
		assertThatThrownBy(() -> DowntimePeriod.calculate(downtimeDays, startDate))
			.isInstanceOf(UserProcedureException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserProcedureErrorCode.INVALID_DOWNTIME_DAYS);
	}

	@Test
	@DisplayName("다운타임 31일 이상 - 예외 발생")
	void calculateExceedMaxDowntime() {
		// given
		int downtimeDays = 31;
		LocalDate startDate = LocalDate.of(2025, 1, 15);

		// when & then
		assertThatThrownBy(() -> DowntimePeriod.calculate(downtimeDays, startDate))
			.isInstanceOf(UserProcedureException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserProcedureErrorCode.INVALID_DOWNTIME_DAYS);
	}

	@Test
	@DisplayName("empty() 메서드 - 항상 동일한 인스턴스 반환")
	void emptyReturnsSameInstance() {
		// when
		DowntimePeriod empty1 = DowntimePeriod.empty();
		DowntimePeriod empty2 = DowntimePeriod.empty();

		// then
		assertThat(empty1).isSameAs(empty2);
		assertThat(empty1.isEmpty()).isTrue();
	}
}
