package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.document.ProcedureDocument;

public interface ProcedureSearchRepository extends ElasticsearchRepository<ProcedureDocument, Long> {
}
