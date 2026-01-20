package com.sopt.cherrish.domain.challenge.core.exception;

/**
 * 챌린지 종료 시 발생하는 예외
 * 트랜잭션 커밋 후 예외를 던져야 하는 경우에 사용
 */
public class ChallengeEndedException extends ChallengeException {

	public ChallengeEndedException() {
		super(ChallengeErrorCode.CHALLENGE_NOT_FOUND);
	}
}
