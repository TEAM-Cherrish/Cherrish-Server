package com.sopt.cherrish.domain.procedure.domain.repository;

import java.util.List;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;

public interface ProcedureRepositoryCustom {

	/**
	 * 시술 검색 (키워드 및 피부 고민 필터링)
	 * - keyword가 null이면 전체 시술 대상
	 * - worryId가 null이면 피부 고민 필터링 안 함
	 * - DISTINCT로 중복 제거 (한 시술이 여러 고민과 매핑되는 경우)
	 */
	List<Procedure> searchProcedures(String keyword, Long worryId);
}
