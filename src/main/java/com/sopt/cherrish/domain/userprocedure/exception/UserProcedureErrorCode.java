package com.sopt.cherrish.domain.userprocedure.exception;

import com.sopt.cherrish.global.response.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserProcedureErrorCode implements ErrorType {
	// UserProcedure 도메인 에러 (UP001 ~ UP099)
	USER_PROCEDURE_NOT_FOUND("UP001", "사용자 시술 일정을 찾을 수 없습니다", 404);

	private final String code;
	private final String message;
	private final int status;
}
