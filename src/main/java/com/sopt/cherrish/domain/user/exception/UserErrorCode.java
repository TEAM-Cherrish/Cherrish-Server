package com.sopt.cherrish.domain.user.exception;

import com.sopt.cherrish.global.response.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorType {
	// User 도메인 에러 (U001 ~ U099)
	USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", 404),
	INVALID_USER_NAME("U002", "유효하지 않은 사용자 이름입니다", 400),
	INVALID_USER_AGE("U003", "유효하지 않은 나이입니다 (1-100세)", 400),
	USER_ALREADY_EXISTS("U004", "이미 존재하는 사용자입니다", 409);

	private final String code;
	private final String message;
	private final int status;
}
