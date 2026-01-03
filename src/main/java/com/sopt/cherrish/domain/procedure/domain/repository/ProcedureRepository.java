package com.sopt.cherrish.domain.procedure.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;

public interface ProcedureRepository extends JpaRepository<Procedure, Long> {

	/**
	 * 시술 검색 (키워드 및 피부 고민 필터링)
	 * - keyword가 null이면 전체 시술 대상
	 * - worryId가 null이면 피부 고민 필터링 안 함
	 * - DISTINCT로 중복 제거 (한 시술이 여러 고민과 매핑되는 경우)
	 */
	@Query("SELECT DISTINCT p FROM Procedure p "
		+ "LEFT JOIN ProcedureWorry pw ON pw.procedure = p "
		+ "WHERE (:keyword IS NULL OR p.name LIKE %:keyword%) AND "
		+ "(:worryId IS NULL OR pw.worry.id = :worryId)")
	List<Procedure> searchProcedures(
		@Param("keyword") String keyword,
		@Param("worryId") Long worryId
	);
}
