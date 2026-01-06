package com.sopt.cherrish.domain.challenge.core.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;

public interface ChallengeStatisticsRepository extends JpaRepository<ChallengeStatistics, Long> {

	/**
	 * 챌린지별 통계 조회
	 * @param challengeId 챌린지 ID
	 * @return 통계 (Optional)
	 */
	Optional<ChallengeStatistics> findByChallengeId(Long challengeId);
}
