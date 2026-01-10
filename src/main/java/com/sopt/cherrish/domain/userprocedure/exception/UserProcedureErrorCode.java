package com.sopt.cherrish.domain.userprocedure.exception;

import com.sopt.cherrish.global.response.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserProcedureErrorCode implements ErrorType {
	// UserProcedure 도메인 에러 (UP001 ~ UP099)
	INVALID_DOWNTIME_DAYS("UP001", "다운타임 일수는 0일 이상 30일 이하여야 합니다", 400);

	private final String code;
	private final String message;
	private final int status;
}
