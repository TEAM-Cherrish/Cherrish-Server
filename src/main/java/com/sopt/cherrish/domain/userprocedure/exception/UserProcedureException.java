package com.sopt.cherrish.domain.userprocedure.exception;

import com.sopt.cherrish.global.exception.BaseException;

public class UserProcedureException extends BaseException {

	public UserProcedureException(UserProcedureErrorCode errorCode) {
		super(errorCode);
	}
}
