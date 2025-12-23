package com.sopt.cherrish.global.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sopt.cherrish.global.swagger.OpenApiConfigurer;
import com.sopt.cherrish.global.swagger.SwaggerErrorExampleGenerator;
import com.sopt.cherrish.global.swagger.SwaggerOperationCustomizer;

import io.swagger.v3.oas.models.OpenAPI;

@Configuration
public class SwaggerConfig {

	private final String baseUrl;

	public SwaggerConfig(@Value("${spring.swagger.base-url}") String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Bean
	public OpenAPI openAPI() {
		return new OpenApiConfigurer(baseUrl).createOpenAPI();
	}

	@Bean
	public OperationCustomizer customize() {
		return new SwaggerOperationCustomizer(
			new SwaggerErrorExampleGenerator()
		);
	}

}
