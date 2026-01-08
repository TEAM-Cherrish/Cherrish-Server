package com.sopt.cherrish.domain.challenge.core.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeRoutineService {

	private final ChallengeRoutineRepository routineRepository;
	private final Clock clock;

	/**
	 * 챌린지 루틴 생성 및 Batch Insert
	 * @param challenge 챌린지
	 * @param routineNames 루틴명 리스트
	 * @return 생성된 루틴 리스트
	 */
	@Transactional
	public List<ChallengeRoutine> createAndSaveRoutines(
		Challenge challenge, List<String> routineNames) {

		// Challenge 엔티티의 팩토리 메서드 사용
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(routineNames);

		// Batch Insert (JPA saveAll 사용)
		return routineRepository.saveAll(routines);
	}

	/**
	 * 오늘의 루틴 조회
	 * @param challengeId 챌린지 ID
	 * @return 오늘의 루틴 리스트
	 */
	public List<ChallengeRoutine> getTodayRoutines(Long challengeId) {
		LocalDate today = LocalDate.now(clock);
		return routineRepository.findByChallengeIdAndScheduledDate(challengeId, today);
	}

	/**
	 * 특정 날짜의 루틴 조회
	 * @param challengeId 챌린지 ID
	 * @param scheduledDate 예정일
	 * @return 루틴 리스트
	 */
	public List<ChallengeRoutine> getRoutinesByDate(Long challengeId, LocalDate scheduledDate) {
		return routineRepository.findByChallengeIdAndScheduledDate(challengeId, scheduledDate);
	}

	/**
	 * 루틴 조회 (Challenge와 Statistics를 함께 로드)
	 * 루틴 완료 토글 시 통계 중복 조회 방지를 위해 사용
	 * @param routineId 루틴 ID
	 * @return 루틴 (Challenge와 Statistics 포함)
	 */
	public ChallengeRoutine getRoutineByIdWithStatistics(Long routineId) {
		return routineRepository.findByIdWithChallengeAndStatistics(routineId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND));
	}

	/**
	 * 루틴 완료 상태 토글 및 통계 업데이트
	 *
	 * 쿼리 최적화:
	 * - Routine, Challenge, Statistics를 Fetch Join으로 한 번에 조회 (쿼리 1)
	 * - 이미 로드된 Statistics 객체를 직접 조작 (중복 조회 없음)
	 * - 총 3개 쿼리 (기존 4개 → 3개로 25% 감소)
	 *
	 * 동시성 제어:
	 * - ChallengeStatistics에 낙관적 락(@Version) 적용
	 * - 동시 수정 시 OptimisticLockingFailureException 발생
	 *
	 * @param userId 사용자 ID (소유자 검증용)
	 * @param routineId 루틴 ID
	 * @return 완료 응답
	 */
	@Transactional
	public RoutineCompletionResponseDto toggleCompletion(Long userId, Long routineId) {
		// 1. 루틴 조회 (Challenge와 Statistics를 함께 Fetch Join으로 로드)
		ChallengeRoutine routine = getRoutineByIdWithStatistics(routineId);

		// 2. 소유자 검증 (도메인 로직)
		routine.getChallenge().validateOwner(userId);

		// 3. 완료 상태 토글
		routine.toggleCompletion();

		// 4. 통계 업데이트
		ChallengeStatistics statistics = routine.getChallenge().getStatistics();
		if (routine.getIsComplete()) {
			statistics.incrementCompletedCount();
		} else {
			statistics.decrementCompletedCount();
		}
		statistics.updateCherryLevel();

		// 5. 응답 생성
		return RoutineCompletionResponseDto.from(routine);
	}
}
