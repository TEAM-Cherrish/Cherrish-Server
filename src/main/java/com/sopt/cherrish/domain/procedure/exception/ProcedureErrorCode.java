package com.sopt.cherrish.domain.procedure.exception;

import com.sopt.cherrish.global.response.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProcedureErrorCode implements ErrorType {
	// Procedure 도메인 에러 (P001 ~ P099)
	INVALID_DOWNTIME_RANGE("P001", "다운타임 범위가 유효하지 않습니다. 최소 다운타임은 최대 다운타임보다 작거나 같아야 합니다", 400),
	INVALID_DOWNTIME_VALUE("P002", "다운타임은 0 이상이어야 합니다", 400),
	PROCEDURE_NOT_FOUND("P003", "시술을 찾을 수 없습니다", 404);

	private final String code;
	private final String message;
	private final int status;
}
