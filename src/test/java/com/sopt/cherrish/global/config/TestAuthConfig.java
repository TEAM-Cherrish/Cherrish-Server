package com.sopt.cherrish.global.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.sopt.cherrish.domain.auth.domain.repository.AccessTokenBlacklistRepository;
import com.sopt.cherrish.domain.auth.infrastructure.jwt.JwtProperties;
import com.sopt.cherrish.domain.auth.infrastructure.jwt.JwtTokenProvider;

@Configuration
@Profile("test")
public class TestAuthConfig {

	@Bean
	@Primary
	public JwtProperties jwtProperties() {
		JwtProperties properties = new JwtProperties();
		properties.setSecretKey("dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZw==");
		properties.setAccessTokenExpiration(1800000L);
		properties.setRefreshTokenExpiration(1209600000L);
		return properties;
	}

	@Bean
	@Primary
	public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
		return Mockito.mock(JwtTokenProvider.class);
	}

	@Bean
	@Primary
	public AccessTokenBlacklistRepository accessTokenBlacklistRepository() {
		return Mockito.mock(AccessTokenBlacklistRepository.class);
	}
}
