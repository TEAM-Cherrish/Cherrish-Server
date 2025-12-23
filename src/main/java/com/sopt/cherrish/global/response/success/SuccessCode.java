package com.sopt.cherrish.global.response.success;

public enum SuccessCode implements SuccessType {
	SUCCESS("S200", "성공");

	private final String code;
	private final String message;

	SuccessCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
