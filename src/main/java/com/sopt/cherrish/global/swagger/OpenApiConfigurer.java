package com.sopt.cherrish.global.swagger;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

public class OpenApiConfigurer {

	private static final String API_TITLE = "SOPT Member API";
	private static final String API_VERSION = "v1.0.0";
	private static final String API_DESCRIPTION = "SOPT 회원 관리 API 문서";

	private final String baseUrl;

	public OpenApiConfigurer(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public OpenAPI createOpenAPI() {
		return new OpenAPI()
			.info(createApiInfo())
			.servers(createServerList());
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
