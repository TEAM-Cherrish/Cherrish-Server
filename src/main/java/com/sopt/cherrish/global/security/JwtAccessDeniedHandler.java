package com.sopt.cherrish.global.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;
import com.sopt.cherrish.global.response.CommonApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 권한이 없는 요청에 대한 처리를 담당하는 Handler.
 *
 * <p>인증은 되었으나 해당 리소스에 대한 권한이 없는 경우 403 Forbidden 응답을 반환합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);

		CommonApiResponse<Void> errorResponse = CommonApiResponse.fail(AuthErrorCode.ACCESS_DENIED);
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
