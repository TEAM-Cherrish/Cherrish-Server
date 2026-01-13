package com.sopt.cherrish.domain.procedure.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sopt.cherrish.domain.procedure.domain.model.ProcedureWorry;

public interface ProcedureWorryRepository extends JpaRepository<ProcedureWorry, Long> {

	@Query("SELECT pw FROM ProcedureWorry pw JOIN FETCH pw.worry WHERE pw.procedure.id IN :procedureIds")
	List<ProcedureWorry> findAllByProcedureIdInWithWorry(@Param("procedureIds") List<Long> procedureIds);

	@Query(
		"SELECT pw FROM ProcedureWorry pw JOIN FETCH pw.worry "
			+ "WHERE pw.procedure.id IN :procedureIds AND pw.worry.id = :worryId"
	)
	List<ProcedureWorry> findAllByProcedureIdInWithWorryAndWorryId(
		@Param("procedureIds") List<Long> procedureIds,
		@Param("worryId") Long worryId
	);
}
