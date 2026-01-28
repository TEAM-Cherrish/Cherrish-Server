package com.sopt.cherrish.domain.auth.infrastructure.social;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;
import com.sopt.cherrish.domain.auth.exception.AuthException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 로그인 토큰 검증 클라이언트.
 *
 * <p>카카오 Access Token을 사용하여 카카오 API에서 사용자 정보를 조회합니다.</p>
 *
 * @see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api">카카오 로그인 API 문서</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAuthClient implements SocialAuthClient {

	private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

	private final RestTemplate restTemplate;

	/**
	 * 카카오 Access Token으로 사용자 정보를 조회합니다.
	 *
	 * <p>카카오 사용자 정보 API(/v2/user/me)를 호출하여 사용자 정보를 가져옵니다.</p>
	 *
	 * @param accessToken 카카오에서 발급한 Access Token
	 * @return 소셜 사용자 정보 (socialId, email, nickname)
	 * @throws AuthException 토큰이 유효하지 않거나 API 호출에 실패한 경우
	 */
	@Override
	public SocialUserInfo getUserInfo(String accessToken) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			HttpEntity<Void> entity = new HttpEntity<>(headers);

			ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
				KAKAO_USER_INFO_URL,
				HttpMethod.GET,
				entity,
				KakaoUserResponse.class
			);

			KakaoUserResponse body = response.getBody();
			if (body == null) {
				throw new AuthException(AuthErrorCode.SOCIAL_AUTH_FAILED);
			}

			return new SocialUserInfo(
				String.valueOf(body.id()),
				body.kakaoAccount() != null ? body.kakaoAccount().email() : null,
				body.properties() != null ? body.properties().nickname() : null
			);
		} catch (HttpClientErrorException e) {
			log.error("Kakao token validation failed: status={}, body={}",
				e.getStatusCode(), e.getResponseBodyAsString());
			throw new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
		} catch (AuthException e) {
			throw e;
		} catch (Exception e) {
			log.error("Kakao auth error: {}", e.getMessage(), e);
			throw new AuthException(AuthErrorCode.SOCIAL_AUTH_FAILED);
		}
	}

	private record KakaoUserResponse(
		Long id,
		@JsonProperty("kakao_account") KakaoAccount kakaoAccount,
		KakaoProperties properties
	) {
	}

	private record KakaoAccount(
		String email
	) {
	}

	private record KakaoProperties(
		String nickname
	) {
	}
}
