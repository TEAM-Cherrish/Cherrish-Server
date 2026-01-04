package com.sopt.cherrish.domain.openai.exception;

import com.sopt.cherrish.global.exception.BaseException;

/**
 * AI 클라이언트 예외
 */
public class AiClientException extends BaseException {

	public AiClientException(AiErrorCode errorCode) {
		super(errorCode);
	}
}
