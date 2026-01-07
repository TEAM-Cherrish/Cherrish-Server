package com.sopt.cherrish.domain.challenge.core.exception;

import com.sopt.cherrish.global.response.error.ErrorType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengeErrorCode implements ErrorType {
	// Challenge 도메인 에러 (CH001 ~ CH099)
	INVALID_HOMECARE_ROUTINE_ID("CH001", "유효하지 않은 홈케어 루틴 ID입니다 (1-6)", 400),
	DUPLICATE_ACTIVE_CHALLENGE("CH002", "이미 진행 중인 챌린지가 있습니다", 409),
	CHALLENGE_NOT_FOUND("CH003", "챌린지를 찾을 수 없습니다", 404),
	ROUTINE_NOT_FOUND("CH004", "루틴을 찾을 수 없습니다", 404),
	UNAUTHORIZED_ACCESS("CH005", "해당 루틴에 대한 권한이 없습니다", 403),
	STATISTICS_NOT_FOUND("CH006", "챌린지 통계를 찾을 수 없습니다", 404);

	private final String code;
	private final String message;
	private final int status;
}
