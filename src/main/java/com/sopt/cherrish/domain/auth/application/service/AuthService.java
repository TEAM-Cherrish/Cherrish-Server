package com.sopt.cherrish.domain.auth.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.auth.domain.repository.AccessTokenBlacklistRepository;
import com.sopt.cherrish.domain.auth.domain.repository.RefreshTokenRepository;
import com.sopt.cherrish.domain.auth.exception.AuthErrorCode;
import com.sopt.cherrish.domain.auth.exception.AuthException;
import com.sopt.cherrish.domain.auth.infrastructure.jwt.JwtTokenProvider;
import com.sopt.cherrish.domain.auth.infrastructure.social.SocialUserInfo;
import com.sopt.cherrish.domain.auth.presentation.dto.request.SocialLoginRequestDto;
import com.sopt.cherrish.domain.auth.presentation.dto.request.TokenRefreshRequestDto;
import com.sopt.cherrish.domain.auth.presentation.dto.response.LoginResponseDto;
import com.sopt.cherrish.domain.auth.presentation.dto.response.TokenResponseDto;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스.
 *
 * <p>소셜 로그인, 토큰 재발급, 로그아웃 기능을 제공합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private static final String DEFAULT_NAME = "사용자";
	private static final int DEFAULT_AGE = 0;

	private final SocialLoginService socialLoginService;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

	/**
	 * 소셜 로그인을 처리합니다.
	 *
	 * <p>소셜 토큰을 검증하고, 신규 사용자인 경우 회원가입을 진행합니다.
	 * 이후 Access Token과 Refresh Token을 발급합니다.</p>
	 *
	 * @param request 소셜 로그인 요청 (provider, token)
	 * @return 로그인 응답 (userId, isNewUser, accessToken, refreshToken)
	 * @throws AuthException 소셜 토큰이 유효하지 않은 경우
	 */
	@Transactional
	public LoginResponseDto login(SocialLoginRequestDto request) {
		SocialUserInfo socialUserInfo = socialLoginService.authenticate(
			request.provider(),
			request.token()
		);

		Optional<User> existingUser = userRepository.findBySocialProviderAndSocialId(
			request.provider(),
			socialUserInfo.socialId()
		);

		boolean isNewUser = existingUser.isEmpty();
		User user;

		if (isNewUser) {
			user = User.builder()
				.name(DEFAULT_NAME)
				.age(DEFAULT_AGE)
				.socialProvider(request.provider())
				.socialId(socialUserInfo.socialId())
				.email(socialUserInfo.email())
				.build();
			user = userRepository.save(user);
		} else {
			user = existingUser.get();
		}

		TokenResponseDto tokens = issueTokenPair(user.getId());

		return new LoginResponseDto(user.getId(), isNewUser, tokens.accessToken(), tokens.refreshToken());
	}

	/**
	 * Refresh Token으로 새로운 토큰 쌍을 발급합니다.
	 *
	 * <p>Refresh Token Rotation(RTR) 방식을 적용하여 재발급시 새로운 Refresh Token도 함께 발급합니다.</p>
	 *
	 * @param request 토큰 재발급 요청 (refreshToken)
	 * @return 새로운 Access Token과 Refresh Token
	 * @throws AuthException Refresh Token이 유효하지 않거나 만료된 경우
	 */
	@Transactional
	public TokenResponseDto refresh(TokenRefreshRequestDto request) {
		String refreshToken = request.refreshToken();

		jwtTokenProvider.validateToken(refreshToken);

		if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
			throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		Long userId = jwtTokenProvider.getUserId(refreshToken);

		if (!userRepository.existsById(userId)) {
			throw new AuthException(AuthErrorCode.USER_NOT_FOUND);
		}

		String storedToken = refreshTokenRepository.findByUserId(userId)
			.orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

		if (!storedToken.equals(refreshToken)) {
			throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		return issueTokenPair(userId);
	}

	/**
	 * 로그아웃을 처리합니다.
	 *
	 * <p>Redis에 저장된 Refresh Token을 삭제하고, Access Token을 블랙리스트에 추가하여
	 * 해당 토큰들로 더 이상 인증이 불가능하게 합니다.</p>
	 *
	 * @param userId 로그아웃할 사용자 ID
	 * @param authorizationHeader Authorization 헤더 값 (Bearer 토큰)
	 */
	@Transactional
	public void logout(Long userId, String authorizationHeader) {
		refreshTokenRepository.deleteByUserId(userId);

		String accessToken = jwtTokenProvider.extractToken(authorizationHeader);
		if (accessToken != null) {
			long remainingExpiration = jwtTokenProvider.getRemainingExpiration(accessToken);
			accessTokenBlacklistRepository.add(accessToken, remainingExpiration);
		}
	}

	private TokenResponseDto issueTokenPair(Long userId) {
		String accessToken = jwtTokenProvider.createAccessToken(userId);
		String refreshToken = jwtTokenProvider.createRefreshToken(userId);

		refreshTokenRepository.save(
			userId,
			refreshToken,
			jwtTokenProvider.getRefreshTokenExpiration()
		);

		return new TokenResponseDto(accessToken, refreshToken);
	}
}
