package com.sopt.cherrish.domain.auth.infrastructure.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sopt.cherrish.domain.auth.domain.repository.AccessTokenBlacklistRepository;
import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;
import com.sopt.cherrish.domain.auth.exception.AuthException;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.global.security.UserPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 기반 인증을 처리하는 필터.
 *
 * <p>모든 요청에서 Authorization 헤더의 Bearer 토큰을 추출하여 검증합니다.
 * 토큰이 유효하면 SecurityContext에 인증 정보를 설정합니다.</p>
 *
 * <p>토큰이 없거나 유효하지 않은 경우 인증을 설정하지 않고 다음 필터로 진행합니다.
 * 보호된 리소스 접근시 {@link com.sopt.cherrish.global.security.JwtAuthenticationEntryPoint}에서
 * 401 응답을 반환합니다.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
		String token = jwtTokenProvider.extractToken(authorizationHeader);

		if (token != null) {
			try {
				jwtTokenProvider.validateToken(token);

				if (accessTokenBlacklistRepository.isBlacklisted(token)) {
					log.debug("Token is blacklisted");
					filterChain.doFilter(request, response);
					return;
				}

				Long userId = jwtTokenProvider.getUserId(token);
				User user = userRepository.findById(userId)
					.orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

				UserPrincipal userPrincipal = UserPrincipal.from(user);
				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
						userPrincipal,
						null,
						userPrincipal.getAuthorities()
					);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (AuthException e) {
				log.debug("JWT authentication failed: {}", e.getMessage());
			}
		}

		filterChain.doFilter(request, response);
	}
}
