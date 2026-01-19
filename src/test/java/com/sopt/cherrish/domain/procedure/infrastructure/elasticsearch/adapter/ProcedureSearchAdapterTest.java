package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.sopt.cherrish.domain.procedure.domain.port.ProcedureSearchPort;
import com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.document.ProcedureDocument;
import com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.repository.ProcedureSearchRepository;

@SpringBootTest
@Testcontainers
@DisplayName("ProcedureSearchAdapter 통합 테스트")
class ProcedureSearchAdapterTest {

	private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:8.17.1";

	@Container
	static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(
		DockerImageName.parse(ELASTICSEARCH_IMAGE))
		.withEnv("discovery.type", "single-node")
		.withEnv("xpack.security.enabled", "false")
		.withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

	@Autowired
	private ProcedureSearchPort procedureSearchPort;

	@Autowired
	private ProcedureSearchRepository procedureSearchRepository;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	@DynamicPropertySource
	static void elasticsearchProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
		registry.add("cherrish.elasticsearch.enabled", () -> true);
	}

	@BeforeEach
	void setUp() {
		procedureSearchRepository.deleteAll();

		// 테스트 데이터 인덱싱
		List<ProcedureDocument> documents = List.of(
			ProcedureDocument.builder().id(1L).name("IPL 레이저").build(),
			ProcedureDocument.builder().id(2L).name("프락셀 레이저").build(),
			ProcedureDocument.builder().id(3L).name("레이저토닝").build(),
			ProcedureDocument.builder().id(4L).name("보톡스").build(),
			ProcedureDocument.builder().id(5L).name("필러 시술").build(),
			ProcedureDocument.builder().id(6L).name("리쥬란 힐러").build()
		);
		procedureSearchRepository.saveAll(documents);

		// 인덱스 갱신 대기
		elasticsearchOperations.indexOps(ProcedureDocument.class).refresh();
	}

	@Test
	@DisplayName("정확한 키워드로 시술 검색")
	void searchByExactKeyword() {
		// when
		List<Long> result = procedureSearchPort.searchByKeyword("레이저");

		// then
		assertThat(result).isNotEmpty();
		assertThat(result).contains(1L, 2L, 3L);
	}

	@Test
	@DisplayName("부분 키워드로 시술 검색")
	void searchByPartialKeyword() {
		// when
		List<Long> result = procedureSearchPort.searchByKeyword("보톡");

		// then
		assertThat(result).contains(4L);
	}

	@Test
	@DisplayName("오타 보정 검색 - 프락세 -> 프락셀")
	void searchWithTypoCorrection() {
		// when
		List<Long> result = procedureSearchPort.searchByKeyword("프락세");

		// then
		assertThat(result).contains(2L);
	}

	@Test
	@DisplayName("빈 키워드 검색 시 빈 결과 반환")
	void searchWithEmptyKeyword() {
		// when
		List<Long> result = procedureSearchPort.searchByKeyword("");

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("null 키워드 검색 시 빈 결과 반환")
	void searchWithNullKeyword() {
		// when
		List<Long> result = procedureSearchPort.searchByKeyword(null);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("매칭되는 시술이 없을 때 빈 결과 반환")
	void searchWithNoMatch() {
		// when
		List<Long> result = procedureSearchPort.searchByKeyword("존재하지않는시술");

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("ES 가용성 확인")
	void checkAvailability() {
		// when
		boolean available = procedureSearchPort.isAvailable();

		// then
		assertThat(available).isTrue();
	}
}
