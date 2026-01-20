package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.procedure.domain.port.ProcedureSearchPort;
import com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.document.ProcedureDocument;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcedureSearchAdapter implements ProcedureSearchPort {

	private static final int MAX_SEARCH_RESULTS = 1000;
	private static final float EXACT_MATCH_BOOST = 100.0f;
	private static final float PREFIX_MATCH_BOOST = 50.0f;
	private static final float CONTAINS_MATCH_BOOST = 30.0f;
	private static final float PHRASE_MATCH_BOOST = 20.0f;
	private static final float ALL_TERMS_MATCH_BOOST = 10.0f;
	private static final float FUZZY_MATCH_BOOST = 0.5f;
	private static final float PARTIAL_MATCH_BOOST = 1.0f;
	private static final int FUZZY_OFF_MAX_LENGTH = 2;
	private static final int FUZZY_1_MAX_LENGTH = 5;

	private final ElasticsearchOperations elasticsearchOperations;

	@Value("${cherrish.elasticsearch.enabled:true}")
	private boolean elasticsearchEnabled;

	@Override
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

	@Override
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
		String trimmedKeyword = keyword.trim();
		BoolQuery.Builder boolQuery = new BoolQuery.Builder();

		// 1. 완전 일치 - 검색어가 시술명과 정확히 같은 경우 (가장 높은 가중치)
		boolQuery.should(new Query.Builder()
			.term(new TermQuery.Builder()
				.field("name.keyword")
				.value(trimmedKeyword)
				.boost(EXACT_MATCH_BOOST)
				.build())
			.build());

		// 2. Prefix 매칭 - 검색어로 시작하는 시술 (높은 가중치)
		boolQuery.should(new Query.Builder()
			.wildcard(new WildcardQuery.Builder()
				.field("name.keyword")
				.value(trimmedKeyword + "*")
				.boost(PREFIX_MATCH_BOOST)
				.build())
			.build());

		// 3. Contains 매칭 - 검색어가 포함된 시술 (한글자 검색 지원)
		boolQuery.should(new Query.Builder()
			.wildcard(new WildcardQuery.Builder()
				.field("name.keyword")
				.value("*" + trimmedKeyword + "*")
				.boost(CONTAINS_MATCH_BOOST)
				.build())
			.build());

		// 4. Phrase 매칭 - 검색어 순서대로 포함 (예: "보톡스 사각" -> "보톡스...사각" 순서로 포함)
		boolQuery.should(new Query.Builder()
			.matchPhrase(new MatchPhraseQuery.Builder()
				.field("name")
				.query(trimmedKeyword)
				.slop(2)
				.boost(PHRASE_MATCH_BOOST)
				.build())
			.build());

		// 5. 모든 단어 포함 - 검색어의 모든 단어가 포함된 결과 (순서 무관)
		boolQuery.should(new Query.Builder()
			.match(new MatchQuery.Builder()
				.field("name")
				.query(trimmedKeyword)
				.operator(Operator.And)
				.boost(ALL_TERMS_MATCH_BOOST)
				.build())
			.build());

		// 6. 부분 일치 - 일부 단어만 매칭되는 경우 (가장 낮은 가중치)
		boolQuery.should(new Query.Builder()
			.match(new MatchQuery.Builder()
				.field("name")
				.query(trimmedKeyword)
				.boost(PARTIAL_MATCH_BOOST)
				.build())
			.build());

		// 7. Fuzzy 매칭 - 오타 허용 (5글자 이상일 때만)
		String fuzziness = determineFuzziness(trimmedKeyword);
		if (fuzziness != null) {
			boolQuery.should(new Query.Builder()
				.match(new MatchQuery.Builder()
					.field("name")
					.query(trimmedKeyword)
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
	 * 키워드 길이에 따른 Fuzzy edit distance 결정 (글자 단위)
	 * - 1-2글자: Fuzzy OFF (짧은 단어 과매칭 방지)
	 * - 3-5글자: Edit Distance 1
	 * - 6글자 이상: Edit Distance 2
	 */
	private String determineFuzziness(String keyword) {
		int length = keyword.length();
		if (length <= FUZZY_OFF_MAX_LENGTH) {
			return null;
		} else if (length <= FUZZY_1_MAX_LENGTH) {
			return "1";
		} else {
			return "2";
		}
	}
}
