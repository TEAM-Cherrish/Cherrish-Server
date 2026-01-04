package com.sopt.cherrish.domain.procedure.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;

public interface ProcedureRepository extends JpaRepository<Procedure, Long>, ProcedureRepositoryCustom {
}
