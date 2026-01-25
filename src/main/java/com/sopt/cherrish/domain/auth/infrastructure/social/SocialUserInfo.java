package com.sopt.cherrish.domain.auth.infrastructure.social;

public record SocialUserInfo(
	String socialId,
	String email,
	String nickname
) {
}
