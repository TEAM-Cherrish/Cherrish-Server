package com.sopt.cherrish.domain.userprocedure.domain.vo;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 다운타임 기간을 표현하는 Value Object
 * 다운타임은 민감기 -> 주의기 -> 회복기 순서로 구성됩니다.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DowntimePeriod {

	private static final int PERIOD_COUNT = 3;
	private static final DowntimePeriod EMPTY = new DowntimePeriod(List.of(), List.of(), List.of());

	private final List<LocalDate> sensitiveDays;
	private final List<LocalDate> cautionDays;
	private final List<LocalDate> recoveryDays;

	/**
	 * 다운타임이 없는 빈 기간 반환
	 */
	public static DowntimePeriod empty() {
		return EMPTY;
	}

	/**
	 * 다운타임 일수와 시작 날짜로부터 다운타임 기간 계산
	 *
	 * @param downtimeDays 총 다운타임 일수
	 * @param startDate 시술 시작 날짜
	 * @return 계산된 다운타임 기간
	 */
	public static DowntimePeriod calculate(Integer downtimeDays, LocalDate startDate) {
		if (downtimeDays == null || downtimeDays <= 0) {
			return empty();
		}

		// 다운타임을 3으로 나누어 기간 계산 (민감기, 주의기, 회복기)
		int baseDays = downtimeDays / PERIOD_COUNT;
		int remainder = downtimeDays % PERIOD_COUNT;

		int sensitiveDaysCount = baseDays + (remainder >= 1 ? 1 : 0);
		int cautionDaysCount = baseDays + (remainder >= 2 ? 1 : 0);
		int recoveryDaysCount = baseDays;

		// 각 기간의 날짜 목록 생성
		List<LocalDate> sensitiveDays = generateDateRange(startDate, sensitiveDaysCount);
		List<LocalDate> cautionDays = generateDateRange(startDate.plusDays(sensitiveDaysCount), cautionDaysCount);
		List<LocalDate> recoveryDays = generateDateRange(
			startDate.plusDays(sensitiveDaysCount + cautionDaysCount),
			recoveryDaysCount
		);

		return new DowntimePeriod(sensitiveDays, cautionDays, recoveryDays);
	}

	private static List<LocalDate> generateDateRange(LocalDate startDate, int days) {
		if (days <= 0) {
			return List.of();
		}
		return IntStream.range(0, days)
			.mapToObj(startDate::plusDays)
			.toList();
	}

	/**
	 * 다운타임 기간이 비어있는지 확인
	 */
	public boolean isEmpty() {
		return sensitiveDays.isEmpty() && cautionDays.isEmpty() && recoveryDays.isEmpty();
	}
}
