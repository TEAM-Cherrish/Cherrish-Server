package com.sopt.cherrish.domain.challenge.exception;

import com.sopt.cherrish.global.response.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengeErrorCode implements ErrorType {
	// Challenge 도메인 에러 (CH001 ~ CH099)
	INVALID_HOMECARE_ROUTINE_ID("CH001", "유효하지 않은 홈케어 루틴 ID입니다 (1-6)", 400),
	AI_SERVICE_UNAVAILABLE("CH002", "AI 서비스를 일시적으로 사용할 수 없습니다", 503);

	private final String code;
	private final String message;
	private final int status;
}
