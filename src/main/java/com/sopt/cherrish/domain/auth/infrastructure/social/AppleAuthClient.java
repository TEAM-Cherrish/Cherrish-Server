package com.sopt.cherrish.domain.auth.infrastructure.social;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;
import com.sopt.cherrish.domain.auth.exception.AuthException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

/**
 * Apple Sign In 토큰 검증 클라이언트.
 *
 * <p>Apple Identity Token을 검증하고 사용자 정보를 추출합니다.
 * Apple 공개키는 24시간 동안 캐싱되어 성능을 최적화합니다.</p>
 *
 * @see <a href="https://developer.apple.com/documentation/sign_in_with_apple">Apple Sign In Documentation</a>
 */
@Slf4j
@Component
public class AppleAuthClient implements SocialAuthClient {

	private static final String APPLE_PUBLIC_KEY_URL = "https://appleid.apple.com/auth/keys";
	private static final String APPLE_ISSUER = "https://appleid.apple.com";
	private static final long CACHE_DURATION_HOURS = 24;

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${social.apple.client-id}")
	private String clientId;

	private volatile ApplePublicKeys cachedKeys;
	private volatile long cacheExpireTime;

	public AppleAuthClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * Apple Identity Token을 검증하고 사용자 정보를 추출합니다.
	 *
	 * <p>토큰의 서명을 Apple 공개키로 검증하고, issuer와 audience를 확인합니다.</p>
	 *
	 * @param identityToken Apple에서 발급한 Identity Token (JWT)
	 * @return 소셜 사용자 정보 (socialId, email)
	 * @throws AuthException 토큰이 유효하지 않거나 검증에 실패한 경우
	 */
	@Override
	public SocialUserInfo getUserInfo(String identityToken) {
		try {
			ApplePublicKeys applePublicKeys = getApplePublicKeys();

			String[] tokenParts = identityToken.split("\\.");
			if (tokenParts.length != 3) {
				throw new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
			}

			String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
			JsonNode headerNode = objectMapper.readTree(headerJson);
			String kid = headerNode.get("kid").asText();
			String alg = headerNode.get("alg").asText();

			ApplePublicKey matchedKey = findMatchingKey(applePublicKeys, kid, alg);

			if (matchedKey == null) {
				log.info("Apple public key not found in cache, refreshing keys for kid: {}", kid);
				applePublicKeys = refreshApplePublicKeys();
				matchedKey = findMatchingKey(applePublicKeys, kid, alg);

				if (matchedKey == null) {
					throw new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
				}
			}

			PublicKey publicKey = generatePublicKey(matchedKey);

			Claims claims = Jwts.parser()
				.verifyWith(publicKey)
				.requireIssuer(APPLE_ISSUER)
				.requireAudience(clientId)
				.build()
				.parseSignedClaims(identityToken)
				.getPayload();

			return new SocialUserInfo(
				claims.getSubject(),
				claims.get("email", String.class),
				null
			);
		} catch (ExpiredJwtException e) {
			log.error("Apple identity token expired");
			throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
		} catch (AuthException e) {
			throw e;
		} catch (Exception e) {
			log.error("Apple auth error: {}", e.getMessage(), e);
			throw new AuthException(AuthErrorCode.SOCIAL_AUTH_FAILED);
		}
	}

	private PublicKey generatePublicKey(ApplePublicKey appleKey) {
		try {
			byte[] nBytes = Base64.getUrlDecoder().decode(appleKey.n());
			byte[] eBytes = Base64.getUrlDecoder().decode(appleKey.e());

			BigInteger modulus = new BigInteger(1, nBytes);
			BigInteger exponent = new BigInteger(1, eBytes);

			RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			return factory.generatePublic(spec);
		} catch (Exception e) {
			log.error("Failed to generate Apple public key: {}", e.getMessage(), e);
			throw new AuthException(AuthErrorCode.SOCIAL_AUTH_FAILED);
		}
	}

	private ApplePublicKeys getApplePublicKeys() {
		long now = System.currentTimeMillis();

		if (cachedKeys != null && now < cacheExpireTime) {
			return cachedKeys;
		}

		return refreshApplePublicKeys();
	}

	private synchronized ApplePublicKeys refreshApplePublicKeys() {
		long now = System.currentTimeMillis();

		if (cachedKeys != null && now < cacheExpireTime) {
			return cachedKeys;
		}

		try {
			ApplePublicKeys keys = restTemplate.getForObject(
				APPLE_PUBLIC_KEY_URL,
				ApplePublicKeys.class
			);

			if (keys == null || keys.keys() == null) {
				if (cachedKeys != null) {
					log.warn("Failed to fetch Apple public keys, using cached keys");
					return cachedKeys;
				}
				throw new AuthException(AuthErrorCode.SOCIAL_AUTH_FAILED);
			}

			cachedKeys = keys;
			cacheExpireTime = now + TimeUnit.HOURS.toMillis(CACHE_DURATION_HOURS);
			log.info("Apple public keys cached successfully, {} keys loaded", keys.keys().size());

			return cachedKeys;
		} catch (RestClientException e) {
			if (cachedKeys != null) {
				log.warn("Failed to refresh Apple public keys: {}, using cached keys", e.getMessage());
				return cachedKeys;
			}
			log.error("Failed to fetch Apple public keys and no cache available: {}", e.getMessage());
			throw new AuthException(AuthErrorCode.SOCIAL_AUTH_FAILED);
		}
	}

	private ApplePublicKey findMatchingKey(ApplePublicKeys keys, String kid, String alg) {
		return keys.keys().stream()
			.filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
			.findFirst()
			.orElse(null);
	}

	private record ApplePublicKeys(
		List<ApplePublicKey> keys
	) {
	}

	private record ApplePublicKey(
		String kty,
		String kid,
		String use,
		String alg,
		String n,
		String e
	) {
	}
}
