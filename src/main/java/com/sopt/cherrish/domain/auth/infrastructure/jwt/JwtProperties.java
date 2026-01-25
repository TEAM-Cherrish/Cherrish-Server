package com.sopt.cherrish.domain.auth.infrastructure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

	private String secretKey;
	private long accessTokenExpiration;
	private long refreshTokenExpiration;
}
