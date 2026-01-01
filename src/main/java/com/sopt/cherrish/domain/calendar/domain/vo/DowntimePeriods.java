package com.sopt.cherrish.domain.calendar.domain.vo;

import java.time.LocalDate;
import java.util.List;

public record DowntimePeriods(
		List<LocalDate> sensitiveDays,
		List<LocalDate> cautionDays,
		List<LocalDate> recoveryDays
) {
}