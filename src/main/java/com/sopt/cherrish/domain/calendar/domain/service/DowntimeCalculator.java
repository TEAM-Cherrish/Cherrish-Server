package com.sopt.cherrish.domain.calendar.domain.service;

import com.sopt.cherrish.domain.calendar.domain.vo.DowntimePeriods;
import com.sopt.cherrish.domain.calendar.exception.CalendarErrorCode;
import com.sopt.cherrish.domain.calendar.exception.CalendarException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class DowntimeCalculator {

	public DowntimePeriods calculate(int downtimeDays, LocalDate startDate) {
		if (downtimeDays < 0) {
			throw new CalendarException(CalendarErrorCode.INVALID_DOWNTIME_DAYS);
		}
		if (startDate == null) {
			throw new CalendarException(CalendarErrorCode.INVALID_START_DATE);
		}

		// 다운타임을 3등분하고 나머지는 앞에서부터 순차 배분
		int baseDay = downtimeDays / 3;
		int remainder = downtimeDays % 3;

		int sensitivePeriod = baseDay + (remainder >= 1 ? 1 : 0);
		int cautionPeriod = baseDay + (remainder >= 2 ? 1 : 0);

		// 민감 기간
		List<LocalDate> sensitiveDays = IntStream.range(0, sensitivePeriod)
				.mapToObj(startDate::plusDays)
				.toList();

		// 주의 기간
		List<LocalDate> cautionDays = IntStream.range(sensitivePeriod, sensitivePeriod + cautionPeriod)
				.mapToObj(startDate::plusDays)
				.toList();

		// 회복 기간
		List<LocalDate> recoveryDays = IntStream.range(sensitivePeriod + cautionPeriod, downtimeDays)
				.mapToObj(startDate::plusDays)
				.toList();

		return new DowntimePeriods(sensitiveDays, cautionDays, recoveryDays);
	}
}
