package com.sopt.cherrish.domain.calendar.exception;

import com.sopt.cherrish.global.response.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CalendarErrorCode implements ErrorType {
	// Calendar 도메인 에러 (CA001 ~ CA099)
	INVALID_YEAR_RANGE("CA001", "유효하지 않은 연도 범위입니다 (2000-2100)", 400),
	INVALID_MONTH_RANGE("CA002", "유효하지 않은 월 범위입니다 (1-12)", 400),
	PROCEDURE_NOT_FOUND("CA003", "시술 정보를 찾을 수 없습니다", 404),
	INVALID_DOWNTIME_DAYS("CA004", "다운타임 일수는 0 이상이어야 합니다", 400),
	INVALID_START_DATE("CA005", "시작 날짜는 null일 수 없습니다", 400);

	private final String code;
	private final String message;
	private final int status;
}
