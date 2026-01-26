package com.sopt.cherrish.global.swagger;

import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

public class OpenApiConfigurer {

	private static final String API_TITLE = "Cherrish API";
	private static final String API_VERSION = "v1.0.0";
	private static final String API_DESCRIPTION = "Cherrish API 문서";
	private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

	private final String baseUrl;

	public OpenApiConfigurer(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public OpenAPI createOpenAPI() {
		return new OpenAPI()
			.info(createApiInfo())
			.servers(createServerList())
			.components(createComponents())
			.addSecurityItem(createSecurityRequirement());
	}

	private Components createComponents() {
		return new Components()
			.addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme());
	}

	private SecurityScheme createSecurityScheme() {
		return new SecurityScheme()
			.name(SECURITY_SCHEME_NAME)
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.description("JWT Access Token을 입력하세요. (Bearer 접두사 불필요)");
	}

	private SecurityRequirement createSecurityRequirement() {
		return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
	}

	private Info createApiInfo() {
		return new Info()
			.title(API_TITLE)
			.version(API_VERSION)
			.description(API_DESCRIPTION);
	}

	private List<Server> createServerList() {
		return List.of(createServer());
	}

	private Server createServer() {
		return new Server()
			.description("Development Server")
			.url(baseUrl);
	}
}
