package com.sopt.cherrish.domain.challenge.core.application.service;

import java.time.Clock;
import java.time.LocalDate;
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
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateItemDto;
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

	// ===== 조회 메서드 =====

	/**
	 * 오늘의 루틴 조회
	 * @param challengeId 챌린지 ID
	 * @return 오늘의 루틴 리스트
	 */
	public List<ChallengeRoutine> getTodayRoutines(Long challengeId) {
		LocalDate today = LocalDate.now(clock);
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

		routine.getChallenge().validateOwner(userId);

		LocalDate today = LocalDate.now(clock);
		routine.validateOperationDateWithinChallengePeriod(today);

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

		if (routine.getIsComplete()) {
			statistics.incrementCompletedCount();
		} else {
			statistics.decrementCompletedCount();
		}

		statistics.updateCherryLevel();
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
		// 1. 요청에서 루틴 ID 추출
		List<Long> routineIds = request.getRoutines().stream()
			.map(RoutineUpdateItemDto::getRoutineId)
			.toList();

		// 2. 모든 루틴 조회 (Fetch Join으로 Challenge + Statistics 포함)
		List<ChallengeRoutine> routines = routineRepository
			.findByIdInWithChallengeAndStatistics(routineIds);

		// 3. 검증: 모든 루틴 존재 확인
		if (routines.size() != routineIds.size()) {
			throw new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND);
		}

		// 4. 검증: 모든 루틴이 같은 챌린지에 속하는지 확인
		Long challengeId = routines.get(0).getChallenge().getId();
		boolean allSameChallenge = routines.stream()
			.allMatch(r -> r.getChallenge().getId().equals(challengeId));

		if (!allSameChallenge) {
			throw new ChallengeException(ChallengeErrorCode.ROUTINES_FROM_DIFFERENT_CHALLENGES);
		}

		// 5. 검증: 소유자 확인 (한 번만)
		Challenge challenge = routines.get(0).getChallenge();
		challenge.validateOwner(userId);

		// 6. 검증: 현재 날짜가 챌린지 기간 내인지 확인
		LocalDate today = LocalDate.now(clock);
		routines.get(0).validateOperationDateWithinChallengePeriod(today);

		// 7. 요청 매핑: routineId → isComplete
		Map<Long, Boolean> updateMap = request.getRoutines().stream()
			.collect(Collectors.toMap(
				RoutineUpdateItemDto::getRoutineId,
				RoutineUpdateItemDto::getIsComplete
			));

		// 8. 상태 변화 추적 (통계 업데이트용)
		int completedDelta = 0;  // 완료 개수 증감

		// 9. 각 루틴의 상태 업데이트
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

		// 10. 통계 업데이트 (변경된 개수만큼 한 번에 반영)
		if (completedDelta != 0) {
			ChallengeStatistics statistics = challenge.getStatistics();

			if (completedDelta > 0) {
				for (int i = 0; i < completedDelta; i++) {
					statistics.incrementCompletedCount();
				}
			} else {
				for (int i = 0; i < -completedDelta; i++) {
					statistics.decrementCompletedCount();
				}
			}

			statistics.updateCherryLevel();
		}

		// 11. JPA dirty checking으로 자동 업데이트
		return RoutineBatchUpdateResponseDto.from(routines);
	}
}
