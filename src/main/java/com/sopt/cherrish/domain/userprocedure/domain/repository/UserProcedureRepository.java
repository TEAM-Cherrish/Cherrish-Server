package com.sopt.cherrish.domain.userprocedure.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

public interface UserProcedureRepository extends JpaRepository<UserProcedure, Long>,
	UserProcedureRepositoryCustom {

	@Query("SELECT up FROM UserProcedure up WHERE up.id = :userProcedureId AND up.user.id = :userId")
	Optional<UserProcedure> findByIdAndUserId(
		@Param("userProcedureId") Long userProcedureId,
		@Param("userId") Long userId
	);
}
