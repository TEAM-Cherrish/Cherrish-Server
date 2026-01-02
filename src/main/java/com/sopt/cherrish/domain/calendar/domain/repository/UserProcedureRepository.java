package com.sopt.cherrish.domain.calendar.domain.repository;

import com.sopt.cherrish.domain.calendar.domain.model.UserProcedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserProcedureRepository extends JpaRepository<UserProcedure, Long> {

	@Query("SELECT up FROM UserProcedure up "
			+ "JOIN FETCH up.procedure "
			+ "JOIN FETCH up.user "
			+ "WHERE up.user.id = :userId "
			+ "AND up.scheduledAt >= :startDateTime "
			+ "AND up.scheduledAt < :endDateTime "
			+ "ORDER BY up.scheduledAt ASC")
	List<UserProcedure> findByUserIdAndScheduledAtBetween(
			@Param("userId") Long userId,
			@Param("startDateTime") LocalDateTime startDateTime,
			@Param("endDateTime") LocalDateTime endDateTime
	);
}
