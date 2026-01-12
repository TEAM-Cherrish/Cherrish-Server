package com.sopt.cherrish.domain.procedure.domain.repository;

import java.util.List;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.domain.model.ProcedureWorry;

public interface ProcedureRepositoryCustom {

	/**
	 * 시술 검색 (키워드 및 피부 고민 필터링)
	 * - keyword가 null이면 전체 시술 대상
	 * - worryId가 null이면 피부 고민 필터링 안 함
	 * - DISTINCT로 중복 제거 (한 시술이 여러 고민과 매핑되는 경우)
	 */
	List<Procedure> searchProcedures(String keyword, Long worryId);

	/**
	 * 시술 ID 목록으로 ProcedureWorry 조회 (Worry fetch join)
	 * - N+1 문제 방지를 위해 Worry를 한 번에 가져옴
	 */
	List<ProcedureWorry> findAllByProcedureIdInWithWorry(List<Long> procedureIds);
}
