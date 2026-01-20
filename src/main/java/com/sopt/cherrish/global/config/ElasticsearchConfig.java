package com.sopt.cherrish.global.config;

import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;

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
			.connectedTo(elasticsearchUri.replace("http://", "").replace("https://", ""))
			.withConnectTimeout(connectTimeoutDuration)
			.withSocketTimeout(socketTimeoutDuration)
			.build();
	}
}
