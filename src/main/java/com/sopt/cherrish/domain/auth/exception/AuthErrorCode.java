package com.sopt.cherrish.domain.auth.exception;

import com.sopt.cherrish.global.response.error.ErrorType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorType {

	UNAUTHORIZED("A001", "인증이 필요합니다", 401),
	INVALID_TOKEN("A002", "유효하지 않은 토큰입니다", 401),
	TOKEN_EXPIRED("A003", "만료된 토큰입니다", 401),
	ACCESS_DENIED("A004", "접근 권한이 없습니다", 403),
	USER_NOT_FOUND("A005", "사용자를 찾을 수 없습니다", 404),

	INVALID_SOCIAL_TOKEN("A010", "유효하지 않은 소셜 토큰입니다", 401),
	SOCIAL_AUTH_FAILED("A011", "소셜 인증에 실패했습니다", 401),
	UNSUPPORTED_SOCIAL_PROVIDER("A012", "지원하지 않는 소셜 로그인 제공자입니다", 400),

	INVALID_REFRESH_TOKEN("A020", "유효하지 않은 리프레시 토큰입니다", 401),
	REFRESH_TOKEN_NOT_FOUND("A021", "리프레시 토큰이 존재하지 않습니다", 401);

	private final String code;
	private final String message;
	private final int status;
}
