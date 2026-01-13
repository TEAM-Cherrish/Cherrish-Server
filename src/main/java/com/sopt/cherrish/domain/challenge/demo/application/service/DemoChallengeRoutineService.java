package com.sopt.cherrish.domain.challenge.demo.application.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateItemRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineBatchUpdateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallengeRoutine;
import com.sopt.cherrish.domain.challenge.demo.domain.repository.DemoChallengeRoutineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DemoChallengeRoutineService {

	private final DemoChallengeRoutineRepository routineRepository;

	/**
	 * 데모 챌린지 루틴 생성 및 Batch Insert
	 */
	@Transactional
	public List<DemoChallengeRoutine> createAndSaveRoutines(DemoChallenge challenge, List<String> routineNames) {
		List<DemoChallengeRoutine> routines = challenge.createChallengeRoutines(routineNames);
		return routineRepository.saveAll(routines);
	}

	/**
	 * 특정 날짜의 루틴 조회
	 */
	public List<DemoChallengeRoutine> getRoutinesByDate(Long demoChallengeId, LocalDate scheduledDate) {
		return routineRepository.findByDemoChallengeIdAndScheduledDate(demoChallengeId, scheduledDate);
	}

	/**
	 * 루틴 완료 상태 토글 (데모 - 통계 즉시 업데이트 안 됨)
	 */
	@Transactional
	public RoutineCompletionResponseDto toggleCompletion(Long userId, Long routineId) {
		DemoChallengeRoutine routine = routineRepository.findByIdWithChallenge(routineId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND));

		routine.getDemoChallenge().validateOwner(userId);

		routine.toggleCompletion();

		return RoutineCompletionResponseDto.from(routine);
	}

	/**
	 * 여러 루틴의 완료 상태 일괄 업데이트 (데모 - 통계 즉시 업데이트 안 됨)
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
		List<DemoChallengeRoutine> routines = fetchAndValidateRoutines(routineIds);

		validateChallenge(routines, userId);

		updateRoutineStates(routines, request);
		// 데모에서는 통계를 즉시 업데이트하지 않음 (advanceDay 시에만 업데이트)

		return RoutineBatchUpdateResponseDto.fromDemoRoutines(routines);
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
	private List<DemoChallengeRoutine> fetchAndValidateRoutines(List<Long> routineIds) {

		if (routineIds.isEmpty()) {
			throw new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND);
		}

		// 중복 ID 검증
		if (routineIds.size() != new HashSet<>(routineIds).size()) {
			throw new ChallengeException(ChallengeErrorCode.DUPLICATE_ROUTINE_IDS);
		}

		List<DemoChallengeRoutine> routines = routineRepository
			.findByIdInWithChallenge(routineIds);

		// 모든 루틴 존재 확인 (빈 리스트 케이스 포함)
		if (routines.size() != routineIds.size()) {
			throw new ChallengeException(ChallengeErrorCode.ROUTINE_NOT_FOUND);
		}

		return routines;
	}

	/**
	 * 챌린지 검증
	 * - 모든 루틴이 같은 챌린지에 속하는지 확인
	 * - 소유자 검증
	 */
	private void validateChallenge(List<DemoChallengeRoutine> routines, Long userId) {
		DemoChallenge challenge = routines.getFirst().getDemoChallenge();

		// 모든 루틴이 같은 챌린지에 속하는지 확인
		validateAllSameChallenge(routines, challenge.getId());

		// 소유자 검증 (한 번만)
		challenge.validateOwner(userId);
	}

	/**
	 * 모든 루틴이 같은 챌린지에 속하는지 검증
	 */
	private void validateAllSameChallenge(List<DemoChallengeRoutine> routines, Long expectedChallengeId) {
		boolean allSameChallenge = routines.stream()
			.allMatch(r -> r.getDemoChallenge().getId().equals(expectedChallengeId));

		if (!allSameChallenge) {
			throw new ChallengeException(ChallengeErrorCode.ROUTINES_FROM_DIFFERENT_CHALLENGES);
		}
	}

	/**
	 * 루틴 상태 업데이트
	 */
	private void updateRoutineStates(List<DemoChallengeRoutine> routines, RoutineUpdateRequestDto request) {
		Map<Long, Boolean> updateMap = request.routines().stream()
			.collect(Collectors.toMap(
				RoutineUpdateItemRequestDto::routineId,
				RoutineUpdateItemRequestDto::isComplete
			));

		for (DemoChallengeRoutine routine : routines) {
			Boolean targetComplete = updateMap.get(routine.getId());
			Boolean currentComplete = routine.getIsComplete();

			// 상태가 변경되는 경우만 처리
			if (!currentComplete.equals(targetComplete)) {
				routine.toggleCompletion();
			}
		}
	}
}
