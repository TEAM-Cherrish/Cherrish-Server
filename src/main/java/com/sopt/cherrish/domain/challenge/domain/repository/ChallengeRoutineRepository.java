package com.sopt.cherrish.domain.challenge.domain.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.challenge.domain.model.ChallengeRoutine;

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
}
