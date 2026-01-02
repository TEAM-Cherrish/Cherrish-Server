package com.sopt.cherrish.domain.user.exception;

import com.sopt.cherrish.global.exception.BaseException;

public class UserException extends BaseException {

	public UserException(UserErrorCode errorCode) {
		super(errorCode);
	}
}
