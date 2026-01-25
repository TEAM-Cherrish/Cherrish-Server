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

	public Long getUserId(String token) {
		Claims claims = parseClaims(token);
		return Long.parseLong(claims.getSubject());
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
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

	public boolean isRefreshToken(String token) {
		Claims claims = parseClaims(token);
		return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
	}

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
