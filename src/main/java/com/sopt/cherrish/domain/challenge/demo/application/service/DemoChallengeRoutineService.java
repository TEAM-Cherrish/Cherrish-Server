package com.sopt.cherrish.domain.challenge.demo.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
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
}
