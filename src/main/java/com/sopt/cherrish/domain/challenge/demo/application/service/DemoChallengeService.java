package com.sopt.cherrish.domain.challenge.demo.application.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.demo.domain.model.DemoChallenge;
import com.sopt.cherrish.domain.challenge.demo.domain.repository.DemoChallengeRepository;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DemoChallengeService {

	private final DemoChallengeRepository demoChallengeRepository;

	/**
	 * 활성 데모 챌린지 중복 검증
	 */
	public void validateNoDuplicateActiveChallenge(Long userId) {
		if (demoChallengeRepository.existsByUserIdAndIsActiveTrue(userId)) {
			throw new ChallengeException(ChallengeErrorCode.DUPLICATE_ACTIVE_CHALLENGE);
		}
	}

	/**
	 * 데모 챌린지 생성 및 저장
	 */
	@Transactional
	public DemoChallenge createChallenge(Long userId, HomecareRoutine routine, LocalDate startDate) {
		DemoChallenge challenge = DemoChallenge.builder()
			.userId(userId)
			.homecareRoutine(routine)
			.title(routine.getDescription())
			.startDate(startDate)
			.build();

		return demoChallengeRepository.save(challenge);
	}

	/**
	 * 활성 데모 챌린지 조회 (통계와 함께 Fetch Join)
	 */
	public DemoChallenge getActiveChallengeWithStatistics(Long userId) {
		return demoChallengeRepository.findActiveChallengeWithStatistics(userId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));
	}

	/**
	 * 활성 데모 챌린지 조회 - Optional 반환
	 * 챌린지가 없어도 예외를 던지지 않음 (Facade에서 사용)
	 */
	public Optional<DemoChallenge> findActiveChallengeWithStatistics(Long userId) {
		return demoChallengeRepository.findActiveChallengeWithStatistics(userId);
	}
}
