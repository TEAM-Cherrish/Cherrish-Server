package com.sopt.cherrish.domain.procedure.exception;

import com.sopt.cherrish.global.response.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProcedureErrorCode implements ErrorType {
	// Procedure 도메인 에러 (P001 ~ P099)
	INVALID_DOWNTIME_RANGE("P001", "다운타임 범위가 유효하지 않습니다. 최소 다운타임은 최대 다운타임보다 작거나 같아야 합니다", 400),
	INVALID_DOWNTIME_VALUE("P002", "다운타임은 0 이상이어야 합니다", 400),
	PROCEDURE_NOT_FOUND("P003", "시술을 찾을 수 없습니다", 404),

	// Elasticsearch 에러 (P100 ~ P199)
	ELASTICSEARCH_INDEX_CREATION_FAILED("P100", "Elasticsearch 인덱스 생성에 실패했습니다", 500),
	ELASTICSEARCH_INDEXING_FAILED("P101", "Elasticsearch 인덱싱에 실패했습니다", 500),
	ELASTICSEARCH_SEARCH_FAILED("P102", "Elasticsearch 검색에 실패했습니다", 500),
	ELASTICSEARCH_CONNECTION_FAILED("P103", "Elasticsearch 연결에 실패했습니다", 503);

	private final String code;
	private final String message;
	private final int status;
}
