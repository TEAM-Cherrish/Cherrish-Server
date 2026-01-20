package com.sopt.cherrish.global.response.error;

public enum ErrorCode implements ErrorType {
	// 공통 에러
	INVALID_INPUT("C001", "입력값이 올바르지 않습니다", 400),
	INVALID_FORMAT("C002", "데이터 형식이 올바르지 않습니다", 400),
	CONCURRENT_UPDATE("C003", "다른 사용자가 동시에 수정했습니다. 다시 시도해주세요.", 409),
	NOT_FOUND("C004", "요청한 리소스를 찾을 수 없습니다", 404),
	METHOD_NOT_ALLOWED("C005", "지원하지 않는 HTTP 메서드입니다", 405),

	INTERNAL_SERVER_ERROR("C999", "서버 내부 오류가 발생했습니다", 500);

	private final String code;
	private final String message;
	private final int status;

	ErrorCode(String code, String message, int status) {
		this.code = code;
		this.message = message;
		this.status = status;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public int getStatus() {
		return status;
	}
}
