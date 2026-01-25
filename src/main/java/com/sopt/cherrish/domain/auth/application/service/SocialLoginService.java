package com.sopt.cherrish.domain.auth.application.service;

import org.springframework.stereotype.Service;

import com.sopt.cherrish.domain.auth.domain.model.SocialProvider;
import com.sopt.cherrish.domain.auth.infrastructure.social.AppleAuthClient;
import com.sopt.cherrish.domain.auth.infrastructure.social.KakaoAuthClient;
import com.sopt.cherrish.domain.auth.infrastructure.social.SocialUserInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

	private final KakaoAuthClient kakaoAuthClient;
	private final AppleAuthClient appleAuthClient;

	public SocialUserInfo authenticate(SocialProvider provider, String token) {
		return switch (provider) {
			case KAKAO -> kakaoAuthClient.getUserInfo(token);
			case APPLE -> appleAuthClient.getUserInfo(token);
		};
	}
}
