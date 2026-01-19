package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.adapter;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureRepository;
import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.procedure.exception.ProcedureException;
import com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.document.ProcedureDocument;
import com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.repository.ProcedureSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcedureIndexingAdapter {

	private static final String INDEX_NAME = "procedures";

	private final ProcedureRepository procedureRepository;
	private final ProcedureSearchRepository procedureSearchRepository;
	private final ElasticsearchOperations elasticsearchOperations;
	private final ObjectMapper objectMapper;

	@Value("${cherrish.elasticsearch.enabled:true}")
	private boolean elasticsearchEnabled;

	@EventListener(ApplicationReadyEvent.class)
	public void reindexOnStartup() {
		if (!elasticsearchEnabled) {
			log.info("Elasticsearch가 비활성화되어 있습니다. 인덱싱을 건너뜁니다.");
			return;
		}

		try {
			reindexAll();
		} catch (ProcedureException e) {
			log.error("애플리케이션 시작 시 시술 인덱싱에 실패했습니다: {}", e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void reindexAll() {
		try {
			createOrUpdateIndex();

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

	public void indexProcedure(Procedure procedure) {
		if (!elasticsearchEnabled) {
			return;
		}

		try {
			ProcedureDocument document = ProcedureDocument.from(procedure);
			procedureSearchRepository.save(document);
			log.debug("시술 인덱싱 완료: id={}", procedure.getId());
		} catch (Exception e) {
			log.warn("시술 인덱싱 실패: id={}, 원인={}", procedure.getId(), e.getMessage());
		}
	}

	public void deleteProcedure(Long procedureId) {
		if (!elasticsearchEnabled) {
			return;
		}

		try {
			procedureSearchRepository.deleteById(procedureId);
			log.debug("시술 인덱스 삭제 완료: id={}", procedureId);
		} catch (Exception e) {
			log.warn("시술 인덱스 삭제 실패: id={}, 원인={}", procedureId, e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void createOrUpdateIndex() {
		IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(INDEX_NAME));

		if (!indexOps.exists()) {
			try {
				ClassPathResource resource = new ClassPathResource("elasticsearch/procedure-index.json");
				Map<String, Object> settings = objectMapper.readValue(resource.getInputStream(), Map.class);

				Document settingsDoc = Document.from((Map<String, Object>) settings.get("settings"));
				Document mappingsDoc = Document.from((Map<String, Object>) settings.get("mappings"));

				indexOps.create(settingsDoc, mappingsDoc);
				log.info("Elasticsearch 인덱스 생성 완료: {}", INDEX_NAME);
			} catch (Exception e) {
				log.error("커스텀 설정으로 인덱스 생성 실패, 기본 설정으로 생성 시도: {}", e.getMessage());
				try {
					indexOps.create();
				} catch (Exception ex) {
					throw new ProcedureException(ProcedureErrorCode.ELASTICSEARCH_INDEX_CREATION_FAILED);
				}
			}
		}
	}
}
