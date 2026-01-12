package com.sopt.cherrish.domain.challenge.demo.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeRoutine;

public interface DemoChallengeRoutineRepository extends JpaRepository<DemoChallengeRoutine, Long> {

	/**
	 * 데모 챌린지의 특정 날짜 루틴 조회
	 * @param demoChallengeId 데모 챌린지 ID
	 * @param scheduledDate 예정일
	 * @return 루틴 리스트
	 */
	List<DemoChallengeRoutine> findByDemoChallengeIdAndScheduledDate(Long demoChallengeId, LocalDate scheduledDate);

	/**
	 * 데모 챌린지의 완료된 루틴 개수 조회
	 * @param demoChallengeId 데모 챌린지 ID
	 * @return 완료된 루틴 개수
	 */
	@Query("SELECT COUNT(r) FROM DemoChallengeRoutine r WHERE r.demoChallenge.id = :demoChallengeId AND r.isComplete = true")
	int countByDemoChallengeIdAndIsCompleteTrue(@Param("demoChallengeId") Long demoChallengeId);

	/**
	 * 루틴 조회 (DemoChallenge와 함께 fetch)
	 * @param id 루틴 ID
	 * @return 루틴 (DemoChallenge 포함)
	 */
	@Query("SELECT r FROM DemoChallengeRoutine r JOIN FETCH r.demoChallenge WHERE r.id = :id")
	Optional<DemoChallengeRoutine> findByIdWithChallenge(@Param("id") Long id);
}
