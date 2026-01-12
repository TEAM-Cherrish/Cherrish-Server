package com.sopt.cherrish.domain.procedure.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sopt.cherrish.domain.procedure.domain.model.ProcedureWorry;

public interface ProcedureWorryRepository extends JpaRepository<ProcedureWorry, Long> {

	@Query("""
		select pw
		from ProcedureWorry pw
		join fetch pw.worry
		where pw.procedure.id in :procedureIds
		""")
	List<ProcedureWorry> findAllByProcedureIdInWithWorry(@Param("procedureIds") List<Long> procedureIds);

	@Query("""
		select pw
		from ProcedureWorry pw
		join fetch pw.worry
		where pw.procedure.id in :procedureIds
		and pw.worry.id = :worryId
		""")
	List<ProcedureWorry> findAllByProcedureIdInWithWorryAndWorryId(
		@Param("procedureIds") List<Long> procedureIds,
		@Param("worryId") Long worryId
	);
}
