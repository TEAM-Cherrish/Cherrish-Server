package com.sopt.cherrish.domain.challenge.core.exception;

import com.sopt.cherrish.global.exception.BaseException;

public class ChallengeException extends BaseException {

	public ChallengeException(ChallengeErrorCode errorCode) {
		super(errorCode);
	}
}
