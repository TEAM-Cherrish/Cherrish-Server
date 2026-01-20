package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.adapter;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureRepository;
import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.procedure.exception.ProcedureException;
import com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.document.ProcedureDocument;
import com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.repository.ProcedureSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Elasticsearch 인덱싱 어댑터
 * <p>
 * 예외 처리 정책:
 * - 개별 문서 인덱싱/삭제 실패: 로그만 남기고 서비스 계속 (검색 품질 저하는 감수)
 * - 전체 재인덱싱/인덱스 생성 실패: ProcedureException 던짐 (시스템 문제)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcedureIndexingAdapter {

	private final ProcedureRepository procedureRepository;
	private final ProcedureSearchRepository procedureSearchRepository;
	private final ElasticsearchOperations elasticsearchOperations;

	@Value("${cherrish.elasticsearch.enabled:true}")
	private boolean elasticsearchEnabled;

	@EventListener(ApplicationReadyEvent.class)
	public void reindexOnStartup() {
		if (isDisabled()) {
			return;
		}

		try {
			boolean indexCreatedOrUpdated = createOrUpdateIndex();
			if (!indexCreatedOrUpdated && procedureSearchRepository.count() > 0) {
				log.info("ES 인덱스에 데이터가 존재합니다. 재인덱싱을 건너뜁니다.");
				return;
			}
			reindexAll(false);
		} catch (ProcedureException e) {
			log.error("애플리케이션 시작 시 시술 인덱싱 실패: {}", e.getMessage(), e);
		}
	}

	/**
	 * 전체 시술 재인덱싱
	 * @throws ProcedureException 인덱싱 실패 시
	 */
	public void reindexAll() {
		reindexAll(true);
	}

	private void reindexAll(boolean ensureIndex) {
		try {
			if (ensureIndex) {
				createOrUpdateIndex();
			}

			List<Procedure> procedures = procedureRepository.findAll();
			List<ProcedureDocument> documents = procedures.stream()
				.map(ProcedureDocument::from)
				.toList();

			procedureSearchRepository.deleteAll();
			procedureSearchRepository.saveAll(documents);

			log.info("시술 {}건 인덱싱 완료", documents.size());
		} catch (Exception e) {
			log.error("시술 인덱싱 실패: {}", e.getMessage(), e);
			throw new ProcedureException(ProcedureErrorCode.ELASTICSEARCH_INDEXING_FAILED);
		}
	}

	/**
	 * 개별 시술 인덱싱 (실패해도 서비스 계속)
	 */
	public void indexProcedure(Procedure procedure) {
		if (isDisabled()) {
			return;
		}

		try {
			procedureSearchRepository.save(ProcedureDocument.from(procedure));
			log.debug("시술 인덱싱 완료: id={}", procedure.getId());
		} catch (Exception e) {
			log.warn("시술 인덱싱 실패 (서비스 계속): id={}, 원인={}", procedure.getId(), e.getMessage());
		}
	}

	/**
	 * 개별 시술 인덱스 삭제 (실패해도 서비스 계속)
	 */
	public void deleteProcedure(Long procedureId) {
		if (isDisabled()) {
			return;
		}

		try {
			procedureSearchRepository.deleteById(procedureId);
			log.debug("시술 인덱스 삭제 완료: id={}", procedureId);
		} catch (Exception e) {
			log.warn("시술 인덱스 삭제 실패 (서비스 계속): id={}, 원인={}", procedureId, e.getMessage());
		}
	}

	private boolean isDisabled() {
		if (!elasticsearchEnabled) {
			log.debug("Elasticsearch가 비활성화되어 있습니다.");
			return true;
		}
		return false;
	}

	private boolean createOrUpdateIndex() {
		IndexOperations indexOps = elasticsearchOperations.indexOps(
			IndexCoordinates.of(ProcedureDocument.INDEX_NAME)
		);

		if (indexOps.exists()) {
			if (!isIndexMappingValid(indexOps)) {
				log.warn("인덱스 매핑이 올바르지 않습니다. 인덱스를 재생성합니다.");
				indexOps.delete();
				createIndexWithSettings(indexOps);
				return true;
			}
			return false;
		}

		createIndexWithSettings(indexOps);
		return true;
	}

	private boolean isIndexMappingValid(IndexOperations indexOps) {
		try {
			Map<String, Object> mapping = indexOps.getMapping();
			if (mapping == null || !mapping.containsKey("properties")) {
				return false;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) mapping.get("properties");
			if (!properties.containsKey("name")) {
				return false;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> nameField = (Map<String, Object>) properties.get("name");
			return nameField.containsKey("fields");
		} catch (Exception e) {
			log.warn("인덱스 매핑 검증 실패: {}", e.getMessage());
			return false;
		}
	}

	private void createIndexWithSettings(IndexOperations indexOps) {
		Document settings = buildIndexSettings();
		Document mappings = buildIndexMappings();

		try {
			indexOps.create(settings, mappings);
			log.info("Elasticsearch 인덱스 생성 완료: {}", ProcedureDocument.INDEX_NAME);
		} catch (Exception e) {
			log.error("인덱스 생성 실패: {}", e.getMessage(), e);
			throw new ProcedureException(ProcedureErrorCode.ELASTICSEARCH_INDEX_CREATION_FAILED);
		}
	}

	private Document buildIndexSettings() {
		return Document.from(Map.of(
			"number_of_shards", 1,
			"number_of_replicas", 0,
			"analysis", Map.of(
				"filter", Map.of(
					"korean_synonym", Map.of(
						"type", "synonym",
						"synonyms_path", "analysis/synonyms.txt",
						"updateable", true
					),
					"nori_posfilter", Map.of(
						"type", "nori_part_of_speech",
						"stoptags", List.of(
							"E", "IC", "J", "MAG", "MAJ", "MM",
							"SP", "SSC", "SSO", "SC", "SE", "XPN",
							"XSA", "XSN", "XSV", "UNA", "NA", "VSV"
						)
					)
				),
				"tokenizer", Map.of(
					"nori_tokenizer", Map.of(
						"type", "nori_tokenizer",
						"decompound_mode", "mixed",
						"user_dictionary", "analysis/userdict_ko.txt"
					)
				),
				"analyzer", Map.of(
					"korean_analyzer", Map.of(
						"type", "custom",
						"tokenizer", "nori_tokenizer",
						"filter", List.of("nori_posfilter", "nori_readingform", "lowercase")
					),
					"korean_search_analyzer", Map.of(
						"type", "custom",
						"tokenizer", "nori_tokenizer",
						"filter", List.of("nori_posfilter", "nori_readingform", "lowercase", "korean_synonym")
					)
				)
			)
		));
	}

	private Document buildIndexMappings() {
		return Document.from(Map.of(
			"properties", Map.of(
				"id", Map.of("type", "long"),
				"name", Map.of(
					"type", "text",
					"analyzer", "korean_analyzer",
					"search_analyzer", "korean_search_analyzer",
					"fields", Map.of(
						"keyword", Map.of("type", "keyword")
					)
				)
			)
		));
	}
}
