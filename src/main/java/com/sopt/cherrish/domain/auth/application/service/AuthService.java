package com.sopt.cherrish.domain.auth.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

		String accessToken = jwtTokenProvider.createAccessToken(user.getId());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

		refreshTokenRepository.save(
			user.getId(),
			refreshToken,
			jwtTokenProvider.getRefreshTokenExpiration()
		);

		return new LoginResponseDto(user.getId(), isNewUser, accessToken, refreshToken);
	}

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

		String newAccessToken = jwtTokenProvider.createAccessToken(userId);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

		refreshTokenRepository.save(
			userId,
			newRefreshToken,
			jwtTokenProvider.getRefreshTokenExpiration()
		);

		return new TokenResponseDto(newAccessToken, newRefreshToken);
	}

	@Transactional
	public void logout(Long userId) {
		refreshTokenRepository.deleteByUserId(userId);
	}
}
