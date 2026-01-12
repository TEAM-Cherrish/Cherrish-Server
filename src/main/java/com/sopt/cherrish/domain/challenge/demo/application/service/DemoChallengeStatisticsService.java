package com.sopt.cherrish.domain.challenge.demo.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeRoutine;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeStatistics;
import com.sopt.cherrish.domain.challenge.demo.domain.repository.DemoChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.demo.domain.repository.DemoChallengeStatisticsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DemoChallengeStatisticsService {

	private final DemoChallengeStatisticsRepository statisticsRepository;
	private final DemoChallengeRoutineRepository routineRepository;

	/**
	 * 데모 챌린지 통계 초기화
	 * @param challenge 데모 챌린지
	 * @param routines 생성된 루틴 리스트
	 */
	@Transactional
	public void initializeStatistics(DemoChallenge challenge, List<DemoChallengeRoutine> routines) {
		int totalRoutineCount = routines.size();

		DemoChallengeStatistics statistics = DemoChallengeStatistics.builder()
			.demoChallenge(challenge)
			.totalRoutineCount(totalRoutineCount)
			.build();

		statisticsRepository.save(statistics);
	}

	/**
	 * 통계 재계산 (다음 날로 넘어가기 버튼 클릭 시)
	 * 완료된 루틴을 COUNT 쿼리로 조회하여 통계를 업데이트합니다.
	 *
	 * @param demoChallengeId 데모 챌린지 ID
	 */
	@Transactional
	public void recalculateStatistics(Long demoChallengeId) {
		DemoChallengeStatistics statistics = statisticsRepository
			.findByDemoChallengeId(demoChallengeId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.STATISTICS_NOT_FOUND));

		// 완료된 루틴 개수 조회 (COUNT 쿼리)
		int completedCount = routineRepository
			.countByDemoChallengeIdAndIsCompleteTrue(demoChallengeId);

		// 통계 업데이트
		statistics.setCompletedCount(completedCount);
		statistics.updateCherryLevel();
	}
}
