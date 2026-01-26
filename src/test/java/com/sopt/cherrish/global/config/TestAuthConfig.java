package com.sopt.cherrish.global.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.sopt.cherrish.domain.auth.domain.repository.AccessTokenBlacklistRepository;
import com.sopt.cherrish.domain.auth.infrastructure.jwt.JwtTokenProvider;

@Configuration
@Profile("test")
public class TestAuthConfig {

	@Bean
	@Primary
	public JwtTokenProvider jwtTokenProvider() {
		return Mockito.mock(JwtTokenProvider.class);
	}

	@Bean
	@Primary
	public AccessTokenBlacklistRepository accessTokenBlacklistRepository() {
		return Mockito.mock(AccessTokenBlacklistRepository.class);
	}
}
