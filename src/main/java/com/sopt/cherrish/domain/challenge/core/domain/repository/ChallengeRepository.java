package com.sopt.cherrish.domain.challenge.core.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

	/**
	 * 사용자의 활성 챌린지 존재 여부 확인
	 * @param userId 사용자 ID
	 * @return 활성 챌린지 존재 여부
	 */
	boolean existsByUserIdAndIsActiveTrue(Long userId);

	/**
	 * 사용자의 활성 챌린지 조회
	 * @param userId 사용자 ID
	 * @return 활성 챌린지 (Optional)
	 */
	Optional<Challenge> findByUserIdAndIsActiveTrue(Long userId);

}
