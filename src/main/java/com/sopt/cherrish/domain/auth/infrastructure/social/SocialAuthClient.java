package com.sopt.cherrish.domain.auth.infrastructure.social;

public interface SocialAuthClient {

	SocialUserInfo getUserInfo(String token);
}
