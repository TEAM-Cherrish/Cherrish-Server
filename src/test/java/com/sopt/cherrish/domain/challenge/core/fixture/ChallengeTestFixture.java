package com.sopt.cherrish.domain.challenge.core.fixture;

import java.time.LocalDate;
import java.util.List;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

public class ChallengeTestFixture {

	// 공통 테스트 상수
	public static final LocalDate FIXED_START_DATE = LocalDate.of(2024, 1, 1);
	public static final Long DEFAULT_USER_ID = 1L;
	public static final String DEFAULT_CHALLENGE_TITLE = "7일 챌린지";

	private ChallengeTestFixture() {
		// Utility class
	}

	public static ChallengeCreateRequestDto createValidChallengeRequest() {
		return new ChallengeCreateRequestDto(
			1,
			"7일 챌린지",
			List.of("아침 세안", "토너 바르기", "크림 바르기")
		);
	}

	public static ChallengeCreateRequestDto createRequestWithEmptyTitle() {
		return new ChallengeCreateRequestDto(
			1,
			"",
			List.of("아침 세안")
		);
	}

	/**
	 * 특정 시작일로 Challenge 생성
	 */
	public static Challenge createChallengeWithStartDate(Long userId, LocalDate startDate) {
		return Challenge.builder()
			.userId(userId)
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title(DEFAULT_CHALLENGE_TITLE)
			.startDate(startDate)
			.build();
	}

	/**
	 * 기본 Challenge 생성 (ID 없음)
	 * 테스트 안정성을 위해 고정된 시작일 사용
	 */
	public static Challenge createDefaultChallenge(Long userId) {
		return createChallengeWithStartDate(userId, FIXED_START_DATE);
	}

	/**
	 * Mock 테스트용 Response 생성 (ID 명시적 지정)
	 */
	public static ChallengeCreateResponseDto createChallengeResponse(
		Challenge challenge, List<ChallengeRoutine> routines, Long challengeId) {
		List<ChallengeRoutineResponseDto> routineDtos = routines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		return new ChallengeCreateResponseDto(
			challengeId,
			challenge.getTitle(),
			challenge.getTotalDays(),
			challenge.getStartDate(),
			challenge.getEndDate(),
			routines.size(),
			routineDtos
		);
	}

	/**
	 * 통합 테스트용 Response 생성 (Challenge의 ID 사용)
	 */
	public static ChallengeCreateResponseDto createChallengeResponse(Challenge challenge, List<ChallengeRoutine> routines) {
		return createChallengeResponse(challenge, routines, challenge.getId());
	}
}
