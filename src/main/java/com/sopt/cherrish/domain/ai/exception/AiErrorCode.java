package com.sopt.cherrish.domain.ai.exception;

import com.sopt.cherrish.global.response.error.ErrorType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OpenAI 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements ErrorType {
	// AI 도메인 에러 (AI001 ~ AI099)
	AI_SERVICE_UNAVAILABLE("AI001", "AI 서비스를 일시적으로 사용할 수 없습니다", 503),
	AI_RESPONSE_PARSING_FAILED("AI002", "AI 응답 파싱에 실패했습니다", 500),
	INVALID_PROMPT_TEMPLATE("AI003", "프롬프트 템플릿이 유효하지 않습니다", 400);

	private final String code;
	private final String message;
	private final int status;
}
