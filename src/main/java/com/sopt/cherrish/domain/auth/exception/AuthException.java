package com.sopt.cherrish.domain.auth.exception;

import com.sopt.cherrish.global.exception.BaseException;

public class AuthException extends BaseException {

	public AuthException(AuthErrorCode errorCode) {
		super(errorCode);
	}
}
