package com.sopt.cherrish.domain.challenge.core.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;

public interface ChallengeRoutineRepository extends JpaRepository<ChallengeRoutine, Long> {

	/**
	 * 챌린지의 특정 날짜 루틴 조회
	 * @param challengeId 챌린지 ID
	 * @param scheduledDate 예정일
	 * @return 루틴 리스트
	 */
	List<ChallengeRoutine> findByChallengeIdAndScheduledDate(Long challengeId, LocalDate scheduledDate);

	/**
	 * 챌린지의 미완료 루틴 조회
	 * @param challengeId 챌린지 ID
	 * @return 미완료 루틴 리스트
	 */
	List<ChallengeRoutine> findByChallengeIdAndIsCompleteFalse(Long challengeId);

	/**
	 * 챌린지의 완료 루틴 개수 조회
	 * @param challengeId 챌린지 ID
	 * @return 완료 개수
	 */
	long countByChallengeIdAndIsCompleteTrue(Long challengeId);

	/**
	 * 루틴 조회 (Challenge와 함께 fetch)
	 * @param id 루틴 ID
	 * @return 루틴 (Challenge 포함)
	 */
	@Query("SELECT r FROM ChallengeRoutine r JOIN FETCH r.challenge WHERE r.id = :id")
	Optional<ChallengeRoutine> findByIdWithChallenge(@Param("id") Long id);
}
