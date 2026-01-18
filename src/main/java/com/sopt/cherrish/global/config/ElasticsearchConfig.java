package com.sopt.cherrish.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "cherrish.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(
	basePackages = "com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch"
)
public class ElasticsearchConfig extends ElasticsearchConfiguration {

	@Value("${spring.elasticsearch.uris:http://localhost:9200}")
	private String elasticsearchUri;

	@Value("${spring.elasticsearch.connection-timeout:5s}")
	private String connectionTimeout;

	@Value("${spring.elasticsearch.socket-timeout:30s}")
	private String socketTimeout;

	@Override
	public ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder()
			.connectedTo(elasticsearchUri.replace("http://", "").replace("https://", ""))
			.withConnectTimeout(java.time.Duration.parse("PT" + connectionTimeout.toUpperCase()))
			.withSocketTimeout(java.time.Duration.parse("PT" + socketTimeout.toUpperCase()))
			.build();
	}
}
