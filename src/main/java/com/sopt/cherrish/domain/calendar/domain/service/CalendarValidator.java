package com.sopt.cherrish.domain.calendar.domain.service;

import com.sopt.cherrish.domain.calendar.exception.CalendarErrorCode;
import com.sopt.cherrish.domain.calendar.exception.CalendarException;
import org.springframework.stereotype.Component;

@Component
public class CalendarValidator {

	private static final int MIN_YEAR = 2000;
	private static final int MAX_YEAR = 2100;
	private static final int MIN_MONTH = 1;
	private static final int MAX_MONTH = 12;

	public void validateYearMonth(int year, int month) {
		validateYear(year);
		validateMonth(month);
	}

	private void validateYear(int year) {
		if (year < MIN_YEAR || year > MAX_YEAR) {
			throw new CalendarException(CalendarErrorCode.INVALID_YEAR_RANGE);
		}
	}

	private void validateMonth(int month) {
		if (month < MIN_MONTH || month > MAX_MONTH) {
			throw new CalendarException(CalendarErrorCode.INVALID_MONTH_RANGE);
		}
	}
}
