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
	 * 챌린지의 특정 날짜 루틴 개수 조회
	 * 커스텀 루틴 추가 시 하루 최대 20개 제한 검증에 사용
	 * @param challengeId 챌린지 ID
	 * @param scheduledDate 예정일
	 * @return 루틴 개수
	 */
	long countByChallengeIdAndScheduledDate(Long challengeId, LocalDate scheduledDate);

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

	/**
	 * 여러 루틴 조회 (Challenge와 Statistics를 함께 fetch)
	 * N+1 쿼리 방지 및 통계 중복 조회 방지
	 * DISTINCT를 사용하여 Fetch Join의 카테시안 곱 방지
	 * INNER JOIN을 사용하여 Statistics가 반드시 존재함을 보장
	 *
	 * 주의사항:
	 * - 모든 루틴이 같은 Challenge를 공유하므로 Statistics는 한 번만 조회됨
	 * - IN 절의 ID 개수가 많을 경우 성능 영향 고려 필요
	 *
	 * @param ids 루틴 ID 리스트
	 * @return 루틴 리스트 (Challenge와 Statistics 포함)
	 */
	@Query("""
		SELECT DISTINCT r FROM ChallengeRoutine r
		JOIN FETCH r.challenge c
		INNER JOIN FETCH c.statistics
		WHERE r.id IN :ids
	""")
	List<ChallengeRoutine> findByIdInWithChallengeAndStatistics(@Param("ids") List<Long> ids);
}
