package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = ProcedureDocument.INDEX_NAME, createIndex = false)
public class ProcedureDocument {

	public static final String INDEX_NAME = "procedures";

	@Id
	private Long id;

	@Field(type = FieldType.Text, analyzer = "korean_analyzer", searchAnalyzer = "korean_search_analyzer")
	private String name;

	@Builder
	private ProcedureDocument(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public static ProcedureDocument from(Procedure procedure) {
		return ProcedureDocument.builder()
			.id(procedure.getId())
			.name(procedure.getName())
			.build();
	}
}
