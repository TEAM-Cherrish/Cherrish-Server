package com.sopt.cherrish.domain.auth.application.service;

import org.springframework.stereotype.Service;

import com.sopt.cherrish.domain.auth.domain.model.SocialProvider;
import com.sopt.cherrish.domain.auth.infrastructure.social.AppleAuthClient;
import com.sopt.cherrish.domain.auth.infrastructure.social.KakaoAuthClient;
import com.sopt.cherrish.domain.auth.infrastructure.social.SocialUserInfo;

import lombok.RequiredArgsConstructor;

/**
 * 소셜 로그인 인증을 처리하는 서비스.
 *
 * <p>소셜 플랫폼에 따라 적절한 인증 클라이언트를 선택하여 토큰을 검증합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class SocialLoginService {

	private final KakaoAuthClient kakaoAuthClient;
	private final AppleAuthClient appleAuthClient;

	/**
	 * 소셜 토큰을 검증하고 사용자 정보를 반환합니다.
	 *
	 * @param provider 소셜 플랫폼 (KAKAO, APPLE)
	 * @param token 소셜 플랫폼에서 발급한 토큰
	 * @return 소셜 사용자 정보
	 * @throws com.sopt.cherrish.domain.auth.exception.AuthException 토큰이 유효하지 않은 경우
	 */
	public SocialUserInfo authenticate(SocialProvider provider, String token) {
		return switch (provider) {
			case KAKAO -> kakaoAuthClient.getUserInfo(token);
			case APPLE -> appleAuthClient.getUserInfo(token);
		};
	}
}
