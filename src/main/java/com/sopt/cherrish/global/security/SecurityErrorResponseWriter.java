package com.sopt.cherrish.global.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;
import com.sopt.cherrish.global.response.CommonApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Security 관련 에러 응답을 작성하는 유틸리티 클래스.
 *
 * <p>인증/인가 실패 시 일관된 JSON 응답을 생성합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

	private final ObjectMapper objectMapper;

	/**
	 * 에러 응답을 HTTP Response에 작성합니다.
	 *
	 * @param response HTTP 응답 객체
	 * @param status HTTP 상태 코드
	 * @param errorCode 에러 코드
	 * @throws IOException 응답 작성 중 예외 발생 시
	 */
	public void writeErrorResponse(
		HttpServletResponse response,
		int status,
		AuthErrorCode errorCode
	) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(status);

		CommonApiResponse<Void> errorResponse = CommonApiResponse.fail(errorCode);
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
