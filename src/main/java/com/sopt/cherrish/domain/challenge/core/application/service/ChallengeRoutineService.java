package com.sopt.cherrish.domain.challenge.core.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateItemRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineBatchUpdateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeRoutineService {

	private final ChallengeRoutineRepository routineRepository;
	private final Clock clock;

	// ===== 생성 메서드 =====

	/**
	 * 챌린지 루틴 생성 및 Batch Insert
	 * @param challenge 챌린지
	 * @param routineNames 루틴명 리스트
	 * @return 생성된 루틴 리스트
	 */
	@Transactional
	public List<ChallengeRoutine> createAndSaveRoutines(Challenge challenge, List<String> routineNames) {
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(routineNames);
		return routineRepository.saveAll(routines);
	}

	/**
	 * 커스텀 루틴 추가 및 Batch Insert
	 * @param challenge 챌린지
	 * @param routineName 루틴명
	 * @param today 현재 날짜
	 * @return 생성된 루틴 리스트
	 */
	@Transactional
	public List<ChallengeRoutine> createAndSaveCustomRoutine(
		Challenge challenge,
		String routineName,
		LocalDate today
	) {
		List<ChallengeRoutine> routines = challenge.createCustomRoutinesFromToday(routineName, today);
		return routineRepository.saveAll(routines);
	}

	// ===== 조회 메서드 =====

	/**
	 * 오늘의 루틴 조회
	 * @param challengeId 챌린지 ID
	 * @return 오늘의 루틴 리스트
	 */
	public List<ChallengeRoutine> getTodayRoutines(Long challengeId) {
		LocalDate today = getCurrentDate();
		return getRoutinesByDate(challengeId, today);
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

	// ===== 수정 메서드 =====

	/**
	 * 루틴 완료 상태 토글 및 통계 업데이트
	 *
	 * 쿼리 최적화:
	 * - Routine, Challenge, Statistics를 Fetch Join으로 한 번에 조회 (쿼리 1개)
	 * - 이미 로드된 Statistics 객체를 직접 조작 (중복 조회 없음)
	 * - 총 3개 쿼리 (조회 1 + 루틴 업데이트 1 + 통계 업데이트 1)
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
		ChallengeRoutine routine = getRoutineByIdWithStatistics(routineId);

		validateRoutineOwnerAndPeriod(routine, userId);

		routine.toggleCompletion();

		updateStatistics(routine);

		return RoutineCompletionResponseDto.from(routine);
	}

	// ===== Private 헬퍼 메서드 =====

	/**
	 * 루틴 조회 (Challenge와 Statistics를 함께 로드)
	 * @param routineId 루틴 ID
	 * @return 루틴 (Challenge와 Statistics 포함)
	 */
	private ChallengeRoutine getRoutineByIdWithStatistics(Long routineId) {
		return routineRepository.findByIdWithChallengeAndStatistics(routineId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND));
	}

	/**
	 * 루틴 완료 상태에 따라 통계 업데이트
	 * @param routine 루틴
	 */
	private void updateStatistics(ChallengeRoutine routine) {
		ChallengeStatistics statistics = routine.getChallenge().getStatistics();

		int delta = routine.getIsComplete() ? 1 : -1;
		statistics.adjustCompletedCount(delta);
		statistics.updateCherryLevel();
	}

	/**
	 * 현재 날짜 조회 (시간 개념 중앙화)
	 */
	private LocalDate getCurrentDate() {
		return LocalDate.now(clock);
	}

	/**
	 * 루틴 소유자 및 작업 가능 날짜 검증 (공통 메서드)
	 */
	private void validateRoutineOwnerAndPeriod(ChallengeRoutine routine, Long userId) {
		routine.getChallenge().validateOwner(userId);

		LocalDate today = getCurrentDate();
		routine.validateOperationDateWithinChallengePeriod(today);
	}

	/**
	 * 여러 루틴의 완료 상태 일괄 업데이트
	 *
	 * 동시성 제어:
	 * - ChallengeStatistics에 낙관적 락(@Version) 적용
	 * - 동시 수정 시 OptimisticLockingFailureException 발생 → 409 Conflict
	 *
	 * @param userId 사용자 ID (소유자 검증용)
	 * @param request 업데이트 요청 (routineId와 isComplete 리스트)
	 * @return 업데이트된 루틴 목록
	 * @throws ChallengeException 검증 실패 시
	 */
	@Transactional
	public RoutineBatchUpdateResponseDto updateMultipleRoutines(
		Long userId,
		RoutineUpdateRequestDto request
	) {
		List<Long> routineIds = extractRoutineIds(request);
		List<ChallengeRoutine> routines = fetchAndValidateRoutines(routineIds);

		Challenge challenge = validateAndGetChallenge(routines, userId);

		int completedDelta = updateRoutineStates(routines, request);
		updateChallengeStatistics(challenge, completedDelta);

		return RoutineBatchUpdateResponseDto.from(routines);
	}

	// ===== Private 헬퍼 메서드 (Batch Update) =====

	/**
	 * 요청에서 루틴 ID 리스트 추출
	 */
	private List<Long> extractRoutineIds(RoutineUpdateRequestDto request) {
		return request.routines().stream()
			.map(RoutineUpdateItemRequestDto::routineId)
			.toList();
	}

	/**
	 * 루틴 조회 및 존재 여부 검증
	 */
	private List<ChallengeRoutine> fetchAndValidateRoutines(List<Long> routineIds) {
		// 중복 ID 검증
		if (routineIds.size() != new HashSet<>(routineIds).size()) {
			throw new ChallengeException(ChallengeErrorCode.DUPLICATE_ROUTINE_IDS);
		}

		List<ChallengeRoutine> routines = routineRepository
			.findByIdInWithChallengeAndStatistics(routineIds);

		// 모든 루틴 존재 확인 (빈 리스트 케이스 포함)
		if (routines.size() != routineIds.size()) {
			throw new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND);
		}

		return routines;
	}

	/**
	 * 챌린지 검증 및 반환
	 * - 모든 루틴이 같은 챌린지에 속하는지 확인
	 * - 소유자 검증
	 * - 챌린지 기간 내 날짜 검증
	 */
	private Challenge validateAndGetChallenge(List<ChallengeRoutine> routines, Long userId) {
		Challenge challenge = routines.getFirst().getChallenge();

		// 모든 루틴이 같은 챌린지에 속하는지 확인
		validateAllSameChallenge(routines, challenge.getId());

		// 소유자 검증 (한 번만)
		challenge.validateOwner(userId);

		// 각 루틴의 날짜 검증 (각 루틴의 scheduledDate가 다를 수 있음)
		LocalDate today = getCurrentDate();
		for (ChallengeRoutine routine : routines) {
			routine.validateOperationDateWithinChallengePeriod(today);
		}

		return challenge;
	}

	/**
	 * 모든 루틴이 같은 챌린지에 속하는지 검증
	 */
	private void validateAllSameChallenge(List<ChallengeRoutine> routines, Long expectedChallengeId) {
		boolean allSameChallenge = routines.stream()
			.allMatch(r -> r.getChallenge().getId().equals(expectedChallengeId));

		if (!allSameChallenge) {
			throw new ChallengeException(ChallengeErrorCode.ROUTINES_FROM_DIFFERENT_CHALLENGES);
		}
	}

	/**
	 * 루틴 상태 업데이트 및 delta 계산
	 */
	private int updateRoutineStates(List<ChallengeRoutine> routines, RoutineUpdateRequestDto request) {
		Map<Long, Boolean> updateMap = request.routines().stream()
			.collect(Collectors.toMap(
				RoutineUpdateItemRequestDto::routineId,
				RoutineUpdateItemRequestDto::isComplete
			));

		int completedDelta = 0;

		for (ChallengeRoutine routine : routines) {
			Boolean targetComplete = updateMap.get(routine.getId());
			Boolean currentComplete = routine.getIsComplete();

			// 상태가 변경되는 경우만 처리
			if (!currentComplete.equals(targetComplete)) {
				if (targetComplete) {
					completedDelta++;  // 미완료 → 완료
				} else {
					completedDelta--;  // 완료 → 미완료
				}
				routine.toggleCompletion();
			}
		}

		return completedDelta;
	}

	/**
	 * 챌린지 통계 업데이트 (delta 기반)
	 */
	private void updateChallengeStatistics(Challenge challenge, int completedDelta) {
		if (completedDelta != 0) {
			ChallengeStatistics statistics = challenge.getStatistics();
			statistics.adjustCompletedCount(completedDelta);
			statistics.updateCherryLevel();
		}
	}
}
