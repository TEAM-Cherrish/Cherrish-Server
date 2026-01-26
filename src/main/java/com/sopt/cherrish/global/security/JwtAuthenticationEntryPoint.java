package com.sopt.cherrish.global.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 인증되지 않은 요청에 대한 처리를 담당하는 EntryPoint.
 *
 * <p>JWT 토큰이 없거나 유효하지 않은 경우 401 Unauthorized 응답을 반환합니다.
 * Spring Security 필터 체인에서 발생하는 인증 예외를 처리합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final SecurityErrorResponseWriter errorResponseWriter;

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException {
		errorResponseWriter.writeErrorResponse(
			response,
			HttpServletResponse.SC_UNAUTHORIZED,
			AuthErrorCode.UNAUTHORIZED
		);
	}
}
