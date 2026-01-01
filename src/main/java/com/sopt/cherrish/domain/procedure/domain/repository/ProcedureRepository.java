package com.sopt.cherrish.domain.procedure.domain.repository;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedureRepository extends JpaRepository<Procedure, Long> {
}
