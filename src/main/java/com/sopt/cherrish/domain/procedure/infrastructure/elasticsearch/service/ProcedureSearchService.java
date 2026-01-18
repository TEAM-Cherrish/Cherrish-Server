package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.document.ProcedureDocument;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcedureSearchService {

	private static final int MAX_SEARCH_RESULTS = 1000;
	private static final float EXACT_MATCH_BOOST = 5.0f;
	private static final float NGRAM_MATCH_BOOST = 1.0f;
	private static final float FUZZY_MATCH_BOOST = 0.4f;
	private static final String MINIMUM_SHOULD_MATCH = "70%";
	private static final int MIN_NGRAM_KEYWORD_LENGTH = 2;

    private final ElasticsearchOperations elasticsearchOperations;

	@Value("${cherrish.elasticsearch.enabled:true}")
	private boolean elasticsearchEnabled;

	public boolean isAvailable() {
		if (!elasticsearchEnabled) {
			return false;
		}
		try {
			return elasticsearchOperations.indexOps(ProcedureDocument.class).exists();
		} catch (Exception e) {
			log.warn("Elasticsearch 연결 불가: {}", e.getMessage());
			return false;
		}
	}

	public List<Long> searchByKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return List.of();
		}

		Query query = buildSearchQuery(keyword);
		NativeQuery searchQuery = NativeQuery.builder()
			.withQuery(query)
			.withMaxResults(MAX_SEARCH_RESULTS)
			.build();

		SearchHits<ProcedureDocument> searchHits = elasticsearchOperations.search(
			searchQuery,
			ProcedureDocument.class
		);

		return searchHits.getSearchHits().stream()
			.map(SearchHit::getContent)
			.map(ProcedureDocument::getId)
			.toList();
	}

	private Query buildSearchQuery(String keyword) {
		String fuzziness = determineFuzziness(keyword);
		int keywordLength = keyword.length();

		BoolQuery.Builder boolQuery = new BoolQuery.Builder();

		// 1. 정확한 매칭 (높은 가중치, 토큰 75% 이상 일치)
		boolQuery.should(new Query.Builder()
			.match(new MatchQuery.Builder()
				.field("name")
				.query(keyword)
				.minimumShouldMatch(MINIMUM_SHOULD_MATCH)
				.boost(EXACT_MATCH_BOOST)
				.build())
			.build());

		// 2. N-gram 매칭 - 3글자 이상일 때만 (짧은 키워드 과매칭 방지)
		if (keywordLength > MIN_NGRAM_KEYWORD_LENGTH) {
			boolQuery.should(new Query.Builder()
				.match(new MatchQuery.Builder()
					.field("name.ngram")
					.query(keyword)
					.minimumShouldMatch(MINIMUM_SHOULD_MATCH)
					.boost(NGRAM_MATCH_BOOST)
					.build())
				.build());
		}

		// 3. Fuzzy 매칭 - 3글자 이상일 때만
		if (fuzziness != null) {
			boolQuery.should(new Query.Builder()
				.match(new MatchQuery.Builder()
					.field("name")
					.query(keyword)
					.fuzziness(fuzziness)
					.prefixLength(1)
					.boost(FUZZY_MATCH_BOOST)
					.build())
				.build());
		}

		boolQuery.minimumShouldMatch("1");

		return new Query.Builder()
			.bool(boolQuery.build())
			.build();
	}

	/**
	 * 키워드 길이에 따른 Fuzzy edit distance 결정
	 * - 1-4글자: Fuzzy OFF (짧은 단어 과매칭 방지)
	 * - 5-7글자: Edit Distance 1
	 * - 8글자 이상: Edit Distance 2
	 */
	private String determineFuzziness(String keyword) {
		int length = keyword.length();
		if (length <= 4) {
			return null; // Fuzzy OFF
		} else if (length <= 7) {
			return "1";
		} else {
			return "2";
		}
	}
}
