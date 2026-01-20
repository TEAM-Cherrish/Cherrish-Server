package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.procedure.domain.port.ProcedureSearchPort;
import com.sopt.cherrish.domain.procedure.domain.port.ProcedureSearchResult;
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

	private static final class SearchLimit {
		private static final int MAX_RESULTS = 1000;

		private SearchLimit() {
		}
	}

	private static final class SearchBoost {
		private static final float EXACT_MATCH = 100.0f;
		private static final float PREFIX_MATCH = 50.0f;
		private static final float CONTAINS_MATCH = 30.0f;
		private static final float PHRASE_MATCH = 20.0f;
		private static final float ALL_TERMS_MATCH = 10.0f;
		private static final float PARTIAL_MATCH = 1.0f;

		private SearchBoost() {
		}
	}

	private final ElasticsearchOperations elasticsearchOperations;

	@Value("${cherrish.elasticsearch.enabled:true}")
	private boolean elasticsearchEnabled;

	@Override
	public ProcedureSearchResult searchByKeyword(String keyword) {
		if (!elasticsearchEnabled) {
			return ProcedureSearchResult.unavailable();
		}
		if (keyword == null || keyword.isBlank()) {
			return ProcedureSearchResult.available(List.of());
		}

		try {
			if (!elasticsearchOperations.indexOps(ProcedureDocument.class).exists()) {
				return ProcedureSearchResult.unavailable();
			}

			Query query = buildSearchQuery(keyword);
			NativeQuery searchQuery = NativeQuery.builder()
				.withQuery(query)
				.withMaxResults(SearchLimit.MAX_RESULTS)
				.build();

			SearchHits<ProcedureDocument> searchHits = elasticsearchOperations.search(
				searchQuery,
				ProcedureDocument.class
			);

			List<Long> ids = searchHits.getSearchHits().stream()
				.map(SearchHit::getContent)
				.map(ProcedureDocument::getId)
				.toList();
			return ProcedureSearchResult.available(ids);
		} catch (Exception e) {
			log.warn("Elasticsearch 검색 실패: {}", e.getMessage());
			return ProcedureSearchResult.unavailable();
		}
	}

	private Query buildSearchQuery(String keyword) {
		String trimmedKeyword = keyword.trim();
		String escapedKeyword = escapeWildcard(trimmedKeyword);
		BoolQuery.Builder boolQuery = new BoolQuery.Builder();

		// 1. 완전 일치 - 검색어가 시술명과 정확히 같은 경우 (가장 높은 가중치)
		boolQuery.should(new Query.Builder()
			.term(new TermQuery.Builder()
					.field("name.keyword")
					.value(trimmedKeyword)
					.boost(SearchBoost.EXACT_MATCH)
					.build())
				.build());

		// 2. Prefix 매칭 - 검색어로 시작하는 시술 (높은 가중치)
			boolQuery.should(new Query.Builder()
				.wildcard(new WildcardQuery.Builder()
					.field("name.keyword")
					.value(escapedKeyword + "*")
					.boost(SearchBoost.PREFIX_MATCH)
					.build())
				.build());

		// 3. Contains 매칭 - 검색어가 포함된 시술 (한글자 검색 지원)
			boolQuery.should(new Query.Builder()
				.wildcard(new WildcardQuery.Builder()
					.field("name.keyword")
					.value("*" + escapedKeyword + "*")
					.boost(SearchBoost.CONTAINS_MATCH)
					.build())
				.build());

		// 4. Phrase 매칭 - 검색어 순서대로 포함 (예: "보톡스 사각" -> "보톡스...사각" 순서로 포함)
		boolQuery.should(new Query.Builder()
			.matchPhrase(new MatchPhraseQuery.Builder()
					.field("name")
					.query(trimmedKeyword)
					.slop(2)
					.boost(SearchBoost.PHRASE_MATCH)
					.build())
				.build());

		// 5. 모든 단어 포함 - 검색어의 모든 단어가 포함된 결과 (순서 무관)
		boolQuery.should(new Query.Builder()
			.match(new MatchQuery.Builder()
					.field("name")
					.query(trimmedKeyword)
					.operator(Operator.And)
					.boost(SearchBoost.ALL_TERMS_MATCH)
					.build())
				.build());

		// 6. 부분 일치 - 일부 단어만 매칭되는 경우 (가장 낮은 가중치)
		boolQuery.should(new Query.Builder()
			.match(new MatchQuery.Builder()
					.field("name")
					.query(trimmedKeyword)
					.boost(SearchBoost.PARTIAL_MATCH)
					.build())
				.build());

		boolQuery.minimumShouldMatch("1");

		return new Query.Builder()
			.bool(boolQuery.build())
			.build();
	}

	private String escapeWildcard(String keyword) {
		return keyword.replace("\\", "\\\\")
			.replace("*", "\\*")
			.replace("?", "\\?");
	}
}
