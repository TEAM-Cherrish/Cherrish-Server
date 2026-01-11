package com.sopt.cherrish.domain.challenge.core.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	/**
	 * 사용자의 활성 챌린지 조회 (통계와 함께 Fetch Join)
	 * N+1 쿼리 방지를 위해 ChallengeStatistics를 함께 로드
	 * @param userId 사용자 ID
	 * @return 활성 챌린지 (통계 포함, Optional)
	 */
	@Query("SELECT c FROM Challenge c LEFT JOIN FETCH c.statistics WHERE c.userId = :userId AND c.isActive = true")
	Optional<Challenge> findActiveChallengeWithStatistics(@Param("userId") Long userId);

	/**
	 * 종료일이 지난 활성 챌린지를 벌크 업데이트로 비활성화
	 * 단일 UPDATE 쿼리로 모든 만료된 챌린지를 한 번에 처리합니다.
	 * @param currentDate 현재 날짜
	 * @return 업데이트된 챌린지 개수
	 */
	@Modifying(clearAutomatically = true)
	@Query("UPDATE Challenge c SET c.isActive = false WHERE c.isActive = true AND c.endDate < :currentDate")
	int bulkUpdateExpiredChallenges(@Param("currentDate") LocalDate currentDate);

}
