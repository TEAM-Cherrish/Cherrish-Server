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
	 * 챌린지의 모든 루틴 조회
	 * @param challengeId 챌린지 ID
	 * @return 루틴 리스트
	 */
	List<ChallengeRoutine> findByChallengeId(Long challengeId);

	/**
	 * 챌린지의 특정 날짜 루틴 조회
	 * @param challengeId 챌린지 ID
	 * @param scheduledDate 예정일
	 * @return 루틴 리스트
	 */
	List<ChallengeRoutine> findByChallengeIdAndScheduledDate(Long challengeId, LocalDate scheduledDate);

	/**
	 * 루틴 조회 (Challenge와 함께 fetch)
	 * @param id 루틴 ID
	 * @return 루틴 (Challenge 포함)
	 */
	@Query("SELECT r FROM ChallengeRoutine r JOIN FETCH r.challenge WHERE r.id = :id")
	Optional<ChallengeRoutine> findByIdWithChallenge(@Param("id") Long id);

	/**
	 * 루틴 조회 (Challenge와 Statistics를 함께 fetch)
	 * N+1 쿼리 방지 및 통계 중복 조회 방지
	 * INNER JOIN을 사용하여 Statistics가 반드시 존재함을 보장
	 * @param id 루틴 ID
	 * @return 루틴 (Challenge와 Statistics 포함)
	 */
	@Query("""
		SELECT r FROM ChallengeRoutine r
		JOIN FETCH r.challenge c
		INNER JOIN FETCH c.statistics
		WHERE r.id = :id
	""")
	Optional<ChallengeRoutine> findByIdWithChallengeAndStatistics(@Param("id") Long id);
}
