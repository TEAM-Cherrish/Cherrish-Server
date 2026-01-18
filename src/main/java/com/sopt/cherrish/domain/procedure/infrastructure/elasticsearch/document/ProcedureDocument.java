package com.sopt.cherrish.domain.procedure.infrastructure.elasticsearch.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "procedures")
@Setting(settingPath = "elasticsearch/procedure-settings.json")
public class ProcedureDocument {

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
