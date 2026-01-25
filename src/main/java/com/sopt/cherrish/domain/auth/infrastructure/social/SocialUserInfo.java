package com.sopt.cherrish.domain.auth.infrastructure.social;

/**
 * 소셜 플랫폼에서 조회한 사용자 정보.
 *
 * @param socialId 소셜 플랫폼의 고유 사용자 ID
 * @param email 사용자 이메일 (없을 수 있음)
 * @param nickname 사용자 닉네임 (없을 수 있음)
 */
public record SocialUserInfo(
	String socialId,
	String email,
	String nickname
) {
}
