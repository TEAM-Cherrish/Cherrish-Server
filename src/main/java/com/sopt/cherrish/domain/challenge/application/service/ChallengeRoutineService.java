package com.sopt.cherrish.domain.challenge.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.domain.repository.ChallengeRoutineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeRoutineService {

	private final ChallengeRoutineRepository routineRepository;

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
		LocalDate today = LocalDate.now();
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
}
