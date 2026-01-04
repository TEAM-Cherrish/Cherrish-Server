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
	INVALID_PROMPT_TEMPLATE("AI003", "프롬프트 템플릿이 유효하지 않습니다", 400),

	// HTTP 관련 에러
	AI_BAD_REQUEST("AI004", "AI 요청이 잘못되었습니다", 400),
	AI_UNAUTHORIZED("AI005", "AI 서비스 인증에 실패했습니다", 401),
	AI_FORBIDDEN("AI006", "AI 서비스 접근 권한이 없습니다", 403),
	AI_NOT_FOUND("AI007", "AI 리소스를 찾을 수 없습니다", 404),
	AI_RATE_LIMIT_EXCEEDED("AI008", "AI 요청 한도를 초과했습니다", 429),
	AI_SERVER_ERROR("AI009", "AI 서버 오류가 발생했습니다", 500),

	// 네트워크 및 연결 관련 에러
	AI_NETWORK_ERROR("AI010", "AI 서비스 연결에 실패했습니다", 503),
	AI_TIMEOUT("AI011", "AI 요청 시간이 초과되었습니다", 504),
	AI_CONNECTION_REFUSED("AI012", "AI 서비스 연결이 거부되었습니다", 503);

	private final String code;
	private final String message;
	private final int status;
}
