package com.sopt.cherrish.global.exception;

import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.error.ErrorType;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
	private final ErrorType errorCode;

	public BaseException(ErrorType errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public BaseException(ErrorCode errorCode, String detail) {
		super(errorCode.getMessage() + " → " + detail);
		this.errorCode = errorCode;
	}

}
