package com.sopt.cherrish.domain.userprocedure.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

public interface UserProcedureRepository extends JpaRepository<UserProcedure, Long>,
	UserProcedureRepositoryCustom {
}
