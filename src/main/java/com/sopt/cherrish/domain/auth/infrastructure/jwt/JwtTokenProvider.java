package com.sopt.cherrish.domain.auth.infrastructure.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;
import com.sopt.cherrish.domain.auth.exception.AuthException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰 생성 및 검증을 담당하는 컴포넌트.
 *
 * <p>Access Token과 Refresh Token을 생성하고, 토큰의 유효성을 검증합니다.
 * 토큰에는 사용자 ID와 토큰 타입(access/refresh)이 포함됩니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private static final String TOKEN_TYPE_CLAIM = "type";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";

	private final JwtProperties jwtProperties;
	private SecretKey secretKey;

	@PostConstruct
	protected void init() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Access Token을 생성합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 생성된 Access Token 문자열
	 */
	public String createAccessToken(Long userId) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
			.issuedAt(now)
			.expiration(expiration)
			.signWith(secretKey)
			.compact();
	}

	/**
	 * Refresh Token을 생성합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 생성된 Refresh Token 문자열
	 */
	public String createRefreshToken(Long userId) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
			.issuedAt(now)
			.expiration(expiration)
			.signWith(secretKey)
			.compact();
	}

	/**
	 * 토큰에서 사용자 ID를 추출합니다.
	 *
	 * @param token JWT 토큰
	 * @return 사용자 ID
	 */
	public Long getUserId(String token) {
		Claims claims = parseClaims(token);
		return Long.parseLong(claims.getSubject());
	}

	/**
	 * 토큰의 유효성을 검증합니다.
	 *
	 * <p>토큰이 유효하면 정상 반환되고, 유효하지 않으면 예외가 발생합니다.</p>
	 *
	 * @param token 검증할 JWT 토큰
	 * @throws AuthException 토큰이 만료되었거나 유효하지 않은 경우
	 */
	public void validateToken(String token) {
		try {
			parseClaims(token);
		} catch (ExpiredJwtException e) {
			log.debug("Expired JWT token: {}", e.getMessage());
			throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
		} catch (UnsupportedJwtException e) {
			log.debug("Unsupported JWT token: {}", e.getMessage());
			throw new AuthException(AuthErrorCode.INVALID_TOKEN);
		} catch (MalformedJwtException e) {
			log.debug("Malformed JWT token: {}", e.getMessage());
			throw new AuthException(AuthErrorCode.INVALID_TOKEN);
		} catch (SecurityException e) {
			log.debug("Invalid JWT signature: {}", e.getMessage());
			throw new AuthException(AuthErrorCode.INVALID_TOKEN);
		} catch (IllegalArgumentException e) {
			log.debug("JWT claims string is empty: {}", e.getMessage());
			throw new AuthException(AuthErrorCode.INVALID_TOKEN);
		}
	}

	/**
	 * 토큰이 Refresh Token인지 확인합니다.
	 *
	 * @param token JWT 토큰
	 * @return Refresh Token이면 true, Access Token이면 false
	 */
	public boolean isRefreshToken(String token) {
		Claims claims = parseClaims(token);
		return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
	}

	/**
	 * Refresh Token의 만료 시간(밀리초)을 반환합니다.
	 *
	 * @return Refresh Token 만료 시간 (밀리초)
	 */
	public long getRefreshTokenExpiration() {
		return jwtProperties.getRefreshTokenExpiration();
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
