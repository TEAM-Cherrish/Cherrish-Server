package com.sopt.cherrish.domain.challenge.core.fixture;

import java.time.LocalDate;

import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.global.config.TestClockConfig;

/**
 * 챌린지 테스트에서 공통으로 사용하는 상수 정의
 * 단위 테스트(ChallengeTestFixture)와 통합 테스트(ChallengeIntegrationTestFixture) 모두에서 참조
 */
public final class ChallengeTestConstants {

	// TestClockConfig의 고정 날짜를 참조하여 일관성 유지
	public static final LocalDate FIXED_START_DATE = TestClockConfig.FIXED_TEST_DATE;
	public static final HomecareRoutine DEFAULT_HOMECARE_ROUTINE = HomecareRoutine.SKIN_CONDITION;

	private ChallengeTestConstants() {
		// Utility class
	}
}
