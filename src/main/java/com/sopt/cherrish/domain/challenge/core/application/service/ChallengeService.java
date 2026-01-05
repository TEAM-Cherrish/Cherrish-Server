package com.sopt.cherrish.domain.challenge.core.application.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

	private final ChallengeRepository challengeRepository;

	/**
	 * 활성 챌린지 중복 검증
	 * @param userId 사용자 ID
	 * @throws ChallengeException 이미 활성 챌린지가 있는 경우
	 */
	public void validateNoDuplicateActiveChallenge(Long userId) {
		if (challengeRepository.existsByUserIdAndIsActiveTrue(userId)) {
			throw new ChallengeException(ChallengeErrorCode.DUPLICATE_ACTIVE_CHALLENGE);
		}
	}

	/**
	 * 챌린지 생성 및 저장
	 * @param userId 사용자 ID
	 * @param routine 홈케어 루틴
	 * @param title 챌린지 제목
	 * @param startDate 시작일
	 * @return 생성된 챌린지
	 */
	public Challenge createChallenge(Long userId, HomecareRoutine routine,
		String title, LocalDate startDate) {
		Challenge challenge = Challenge.builder()
			.userId(userId)
			.homecareRoutine(routine)
			.title(title)
			.startDate(startDate)
			.build();

		return challengeRepository.save(challenge);
	}

	/**
	 * 활성 챌린지 조회
	 * @param userId 사용자 ID
	 * @return 활성 챌린지
	 * @throws ChallengeException 활성 챌린지가 없는 경우
	 */
	public Challenge getActiveChallenge(Long userId) {
		return challengeRepository.findByUserIdAndIsActiveTrue(userId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));
	}

	/**
	 * ID로 챌린지 조회
	 * @param challengeId 챌린지 ID
	 * @return 챌린지
	 * @throws ChallengeException 챌린지를 찾을 수 없는 경우
	 */
	public Challenge getChallengeById(Long challengeId) {
		return challengeRepository.findById(challengeId)
			.orElseThrow(() -> new ChallengeException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));
	}
}
