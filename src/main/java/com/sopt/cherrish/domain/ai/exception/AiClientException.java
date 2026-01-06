package com.sopt.cherrish.domain.ai.exception;

import com.sopt.cherrish.global.exception.BaseException;

/**
 * AI 클라이언트 예외
 */
public class AiClientException extends BaseException {

	public AiClientException(AiErrorCode errorCode) {
		super(errorCode);
	}

	/**
	 * @param errorCode AI 에러 코드
	 * @param cause 원본 예외
	 */
	public AiClientException(AiErrorCode errorCode, Throwable cause) {
		super(errorCode);
		initCause(cause);
	}
}
