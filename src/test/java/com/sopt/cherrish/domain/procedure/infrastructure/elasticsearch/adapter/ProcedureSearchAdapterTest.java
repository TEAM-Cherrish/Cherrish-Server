package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
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
		registry.add("spring.elasticsearch.connection-timeout", () -> "5s");
		registry.add("spring.elasticsearch.socket-timeout", () -> "30s");
		registry.add("cherrish.elasticsearch.enabled", () -> true);
	}

	@BeforeEach
	void setUp() {
		// 테스트용 인덱스 생성 (nori 플러그인 없이 기본 analyzer 사용)
		createTestIndex();
		procedureSearchRepository.deleteAll();

		// 실제 DB 시술명으로 테스트 데이터 구성
		List<ProcedureDocument> documents = List.of(
			ProcedureDocument.builder().id(1L).name("보톡스").build(),
			ProcedureDocument.builder().id(2L).name("슈링크").build(),
			ProcedureDocument.builder().id(3L).name("인모드").build(),
			ProcedureDocument.builder().id(4L).name("온다 리프팅").build(),
			ProcedureDocument.builder().id(5L).name("써마지").build(),
			ProcedureDocument.builder().id(6L).name("티타늄").build(),
			ProcedureDocument.builder().id(7L).name("울쎄라").build(),
			ProcedureDocument.builder().id(8L).name("쥬베룩").build(),
			ProcedureDocument.builder().id(9L).name("실 리프팅").build(),
			ProcedureDocument.builder().id(10L).name("아쿠아필").build(),
			ProcedureDocument.builder().id(11L).name("라라필").build(),
			ProcedureDocument.builder().id(12L).name("LDM").build(),
			ProcedureDocument.builder().id(13L).name("세라필").build(),
			ProcedureDocument.builder().id(14L).name("크라이오셀").build(),
			ProcedureDocument.builder().id(15L).name("리쥬란").build(),
			ProcedureDocument.builder().id(16L).name("점 제거").build(),
			ProcedureDocument.builder().id(17L).name("피코 토닝").build(),
			ProcedureDocument.builder().id(18L).name("제네시스 토닝").build(),
			ProcedureDocument.builder().id(19L).name("레이저 토닝").build(),
			ProcedureDocument.builder().id(20L).name("사각턱 보톡스").build(),
			ProcedureDocument.builder().id(21L).name("침샘 보톡스").build(),
			ProcedureDocument.builder().id(22L).name("윤곽 주사").build(),
			ProcedureDocument.builder().id(23L).name("얼굴지방분해주사").build(),
			ProcedureDocument.builder().id(24L).name("자갈턱 보톡스").build(),
			ProcedureDocument.builder().id(25L).name("승모근 보톡스").build(),
			ProcedureDocument.builder().id(26L).name("종아리 보톡스").build(),
			ProcedureDocument.builder().id(27L).name("허벅지 보톡스").build(),
			ProcedureDocument.builder().id(28L).name("넥라인 보톡스").build(),
			ProcedureDocument.builder().id(29L).name("포텐자").build(),
			ProcedureDocument.builder().id(30L).name("스킨보톡스").build(),
			ProcedureDocument.builder().id(31L).name("피코 프락셀").build()
		);
		procedureSearchRepository.saveAll(documents);

		// 인덱스 갱신 대기
		elasticsearchOperations.indexOps(ProcedureDocument.class).refresh();
	}

	private void createTestIndex() {
		IndexOperations indexOps = elasticsearchOperations.indexOps(
			IndexCoordinates.of(ProcedureDocument.INDEX_NAME)
		);

		// 기존 인덱스 삭제
		if (indexOps.exists()) {
			indexOps.delete();
		}

		// nori 플러그인 없이 기본 analyzer를 사용하는 간단한 인덱스 생성
		Document settings = Document.from(Map.of(
			"number_of_shards", 1,
			"number_of_replicas", 0
		));

		Document mappings = Document.from(Map.of(
			"properties", Map.of(
				"id", Map.of("type", "long"),
				"name", Map.of(
					"type", "text",
					"analyzer", "standard",
					"fields", Map.of(
						"keyword", Map.of("type", "keyword")
					)
				)
			)
		));

		indexOps.create(settings, mappings);
	}

	@Test
	@DisplayName("ES 가용성 확인")
	void checkAvailability() {
		// when
		boolean available = procedureSearchPort.searchByKeyword("보톡스").isAvailable();

		// then
		assertThat(available).isTrue();
	}

	@Nested
	@DisplayName("한글자 검색")
	class SingleCharacterSearch {

		@Test
		@DisplayName("'필' 검색 시 아쿠아필, 라라필, 세라필 반환")
		void searchByPil() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("필").procedureIds();

			// then
			assertThat(result).containsExactlyInAnyOrder(10L, 11L, 13L);
		}

		@Test
		@DisplayName("'주' 검색 시 주사 관련 시술 반환")
		void searchByJu() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("주").procedureIds();

			// then
			assertThat(result).contains(22L, 23L); // 윤곽 주사, 얼굴지방분해주사
		}
	}

	@Nested
	@DisplayName("완전 일치 우선 정렬")
	class ExactMatchPriority {

		@Test
		@DisplayName("'보톡스' 검색 시 '보톡스'가 맨 위에 반환")
		void searchBotoxExactMatch() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("보톡스").procedureIds();

			// then
			assertThat(result).isNotEmpty();
			assertThat(result.get(0)).isEqualTo(1L); // 보톡스가 맨 위
			assertThat(result).contains(20L, 21L, 24L, 25L, 26L, 27L, 28L, 30L); // 다른 보톡스들도 포함
		}

		@Test
		@DisplayName("'토닝' 검색 시 토닝 관련 시술 반환")
		void searchToning() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("토닝").procedureIds();

			// then
			assertThat(result).containsExactlyInAnyOrder(17L, 18L, 19L);
		}
	}

	@Nested
	@DisplayName("복합 키워드 검색")
	class MultiKeywordSearch {

		@Test
		@DisplayName("'사각턱 보톡스' 검색 시 사각턱 보톡스가 맨 위에 반환")
		void searchSagakBotox() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("사각턱 보톡스").procedureIds();

			// then
			assertThat(result).isNotEmpty();
			assertThat(result.get(0)).isEqualTo(20L); // 사각턱 보톡스가 맨 위
		}

		@Test
		@DisplayName("'종아리 보톡스' 검색 시 종아리 보톡스가 맨 위에 반환")
		void searchCaflBotox() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("종아리 보톡스").procedureIds();

			// then
			assertThat(result).isNotEmpty();
			assertThat(result.get(0)).isEqualTo(26L); // 종아리 보톡스가 맨 위
		}

		@Test
		@DisplayName("'피코' 검색 시 피코 토닝, 피코 프락셀 반환")
		void searchPico() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("피코").procedureIds();

			// then
			assertThat(result).containsExactlyInAnyOrder(17L, 31L);
		}
	}

	@Nested
	@DisplayName("부분 일치 검색")
	class PartialMatchSearch {

		@Test
		@DisplayName("'리프팅' 검색 시 리프팅 관련 시술 반환")
		void searchLifting() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("리프팅").procedureIds();

			// then
			assertThat(result).contains(4L, 9L); // 온다 리프팅, 실 리프팅
		}

		@Test
		@DisplayName("'레이저' 검색 시 레이저 토닝 반환")
		void searchLaser() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("레이저").procedureIds();

			// then
			assertThat(result).contains(19L); // 레이저 토닝
		}
	}

	@Nested
	@DisplayName("엣지 케이스")
	class EdgeCases {

		@Test
		@DisplayName("빈 키워드 검색 시 빈 결과 반환")
		void searchWithEmptyKeyword() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("").procedureIds();

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("null 키워드 검색 시 빈 결과 반환")
		void searchWithNullKeyword() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword(null).procedureIds();

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("매칭되는 시술이 없을 때 빈 결과 반환")
		void searchWithNoMatch() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("존재하지않는시술").procedureIds();

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("공백만 있는 키워드 검색 시 빈 결과 반환")
		void searchWithWhitespace() {
			// when
			List<Long> result = procedureSearchPort.searchByKeyword("   ").procedureIds();

			// then
			assertThat(result).isEmpty();
		}
	}
}
