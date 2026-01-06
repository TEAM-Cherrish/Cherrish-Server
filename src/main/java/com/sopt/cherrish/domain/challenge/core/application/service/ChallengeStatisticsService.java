package com.sopt.cherrish.domain.challenge.core.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeStatisticsRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeStatisticsService {

	private final ChallengeStatisticsRepository statisticsRepository;

	/**
	 * 챌린지 통계 초기화
	 * @param challenge 챌린지
	 * @param routines 생성된 루틴 리스트
	 */
	@Transactional
	public void initializeStatistics(Challenge challenge, List<ChallengeRoutine> routines) {
		int totalRoutineCount = routines.size();

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(totalRoutineCount)
			.build();

		statisticsRepository.save(statistics);
	}

	/**
	 * 통계 업데이트 (완료 개수 증가)
	 * @param challengeId 챌린지 ID
	 */
	@Transactional
	public void incrementCompletedCount(Long challengeId) {
		ChallengeStatistics statistics = statisticsRepository
			.findByChallengeId(challengeId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

		statistics.incrementCompletedCount();
	}

	/**
	 * 통계 업데이트 (완료 개수 감소)
	 * @param challengeId 챌린지 ID
	 */
	@Transactional
	public void decrementCompletedCount(Long challengeId) {
		ChallengeStatistics statistics = statisticsRepository
			.findByChallengeId(challengeId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

		statistics.decrementCompletedCount();
	}

	/**
	 * 챌린지 통계 조회
	 * @param challengeId 챌린지 ID
	 * @return 통계
	 */
	public ChallengeStatistics getStatistics(Long challengeId) {
		return statisticsRepository.findByChallengeId(challengeId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));
	}
}
