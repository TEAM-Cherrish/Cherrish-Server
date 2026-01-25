package com.sopt.cherrish.domain.auth.infrastructure.social;

/**
 * 소셜 로그인 인증 클라이언트 인터페이스.
 *
 * <p>각 소셜 플랫폼(카카오, 애플 등)별로 구현체를 제공합니다.</p>
 */
public interface SocialAuthClient {

	/**
	 * 소셜 토큰을 검증하고 사용자 정보를 조회합니다.
	 *
	 * @param token 소셜 플랫폼에서 발급한 토큰
	 * @return 소셜 사용자 정보
	 * @throws com.sopt.cherrish.domain.auth.exception.AuthException 토큰이 유효하지 않은 경우
	 */
	SocialUserInfo getUserInfo(String token);
}
