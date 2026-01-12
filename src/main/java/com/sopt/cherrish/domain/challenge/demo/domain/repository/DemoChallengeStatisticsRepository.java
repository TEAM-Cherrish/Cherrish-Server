package com.sopt.cherrish.domain.challenge.demo.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeStatistics;

public interface DemoChallengeStatisticsRepository extends JpaRepository<DemoChallengeStatistics, Long> {

	/**
	 * 데모 챌린지 ID로 통계 조회
	 * @param demoChallengeId 데모 챌린지 ID
	 * @return 통계 (Optional)
	 */
	Optional<DemoChallengeStatistics> findByDemoChallengeId(Long demoChallengeId);
}
