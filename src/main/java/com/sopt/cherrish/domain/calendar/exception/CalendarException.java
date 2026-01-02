package com.sopt.cherrish.domain.calendar.exception;

import com.sopt.cherrish.global.exception.BaseException;

public class CalendarException extends BaseException {

	public CalendarException(CalendarErrorCode errorCode) {
		super(errorCode);
	}
}
