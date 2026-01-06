package com.sopt.cherrish.domain.procedure.exception;

import com.sopt.cherrish.global.exception.BaseException;

public class ProcedureException extends BaseException {

	public ProcedureException(ProcedureErrorCode errorCode) {
		super(errorCode);
	}
}
