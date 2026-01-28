package com.sopt.cherrish.domain.auth.infrastructure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

	@NotBlank
	private String secretKey;

	@Positive
	private long accessTokenExpiration;

	@Positive
	private long refreshTokenExpiration;
}
