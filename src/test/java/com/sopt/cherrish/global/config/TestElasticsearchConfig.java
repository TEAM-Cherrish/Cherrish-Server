package com.sopt.cherrish.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestElasticsearchConfig extends ElasticsearchConfiguration {

	private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:8.17.1";

	@Bean(initMethod = "start", destroyMethod = "stop")
	public ElasticsearchContainer elasticsearchContainer() {
		return new ElasticsearchContainer(DockerImageName.parse(ELASTICSEARCH_IMAGE))
			.withEnv("discovery.type", "single-node")
			.withEnv("xpack.security.enabled", "false")
			.withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
	}

	@Override
	public ClientConfiguration clientConfiguration() {
		ElasticsearchContainer container = elasticsearchContainer();
		return ClientConfiguration.builder()
			.connectedTo(container.getHttpHostAddress())
			.build();
	}
}
