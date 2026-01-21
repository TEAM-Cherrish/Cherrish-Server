package com.sopt.cherrish.global.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "cherrish.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@EnableElasticsearchRepositories(
	basePackages = "com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch"
)
public class ElasticsearchConfig extends ElasticsearchConfiguration {

	@Value("${spring.elasticsearch.uris}")
	private String elasticsearchUri;

	@Value("${spring.elasticsearch.connection-timeout}")
	private String connectionTimeout;

	@Value("${spring.elasticsearch.socket-timeout}")
	private String socketTimeout;

	@Override
	public ClientConfiguration clientConfiguration() {
		Duration connectTimeoutDuration = DurationStyle.detectAndParse(connectionTimeout);
		Duration socketTimeoutDuration = DurationStyle.detectAndParse(socketTimeout);

		return ClientConfiguration.builder()
			.connectedTo(parseHostAndPort(elasticsearchUri))
			.withConnectTimeout(connectTimeoutDuration)
			.withSocketTimeout(socketTimeoutDuration)
			.build();
	}

	private String parseHostAndPort(String uriString) {
		if (uriString == null || uriString.isBlank()) {
			return uriString;
		}
		String trimmed = uriString.trim();
		if (!trimmed.contains("://")) {
			return trimmed;
		}

		URI uri = URI.create(trimmed);
		String host = uri.getHost();
		int port = uri.getPort();
		if (host == null) {
			return trimmed;
		}
		return port > 0 ? host + ":" + port : host;
	}
}
