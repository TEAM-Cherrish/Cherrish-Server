package com.sopt.cherrish.domain.challenge.demo.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;

public interface DemoChallengeRepository extends JpaRepository<DemoChallenge, Long> {

	/**
	 * 사용자의 활성 데모 챌린지 존재 여부 확인
	 * @param userId 사용자 ID
	 * @return 활성 챌린지 존재 여부
	 */
	boolean existsByUserIdAndIsActiveTrue(Long userId);


	/**
	 * 사용자의 활성 데모 챌린지 조회 (통계와 함께 Fetch Join)
	 * @param userId 사용자 ID
	 * @return 활성 챌린지 (통계 포함, Optional)
	 */
	@Query("SELECT dc FROM DemoChallenge dc LEFT JOIN FETCH dc.statistics WHERE dc.userId = :userId AND dc.isActive = true")
	Optional<DemoChallenge> findActiveChallengeWithStatistics(@Param("userId") Long userId);
}
