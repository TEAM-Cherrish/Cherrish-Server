package com.sopt.cherrish.domain.auth.response.success;

import com.sopt.cherrish.global.response.success.SuccessType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements SuccessType {

	LOGIN_SUCCESS("A200", "로그인 성공"),
	TOKEN_REFRESHED("A201", "토큰 재발급 성공"),
	LOGOUT_SUCCESS("A202", "로그아웃 성공");

	private final String code;
	private final String message;
}
