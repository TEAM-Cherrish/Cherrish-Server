package com.sopt.cherrish.domain.auth.infrastructure.social;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;
import com.sopt.cherrish.domain.auth.exception.AuthException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleAuthClient implements SocialAuthClient {

	private static final String APPLE_PUBLIC_KEY_URL = "https://appleid.apple.com/auth/keys";
	private static final String APPLE_ISSUER = "https://appleid.apple.com";

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${social.apple.client-id}")
	private String clientId;

	@Override
	public SocialUserInfo getUserInfo(String identityToken) {
		try {
			ApplePublicKeys applePublicKeys = restTemplate.getForObject(
				APPLE_PUBLIC_KEY_URL,
				ApplePublicKeys.class
			);

			if (applePublicKeys == null || applePublicKeys.keys() == null) {
				throw new AuthException(AuthErrorCode.SOCIAL_AUTH_FAILED);
			}

			String[] tokenParts = identityToken.split("\\.");
			if (tokenParts.length != 3) {
				throw new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
			}

			String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
			JsonNode headerNode = objectMapper.readTree(headerJson);
			String kid = headerNode.get("kid").asText();
			String alg = headerNode.get("alg").asText();

			ApplePublicKey matchedKey = applePublicKeys.keys().stream()
				.filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
				.findFirst()
				.orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN));

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
