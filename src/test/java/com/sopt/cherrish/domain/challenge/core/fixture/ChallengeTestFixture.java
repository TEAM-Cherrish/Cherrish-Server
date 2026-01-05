package com.sopt.cherrish.domain.challenge.core.fixture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

public class ChallengeTestFixture {

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

	public static ChallengeCreateRequestDto createRequestWithInvalidHomecareRoutineId() {
		return new ChallengeCreateRequestDto(
			999,
			"7일 챌린지",
			List.of("아침 세안")
		);
	}

	public static ChallengeCreateRequestDto createRequestWithEmptyTitle() {
		return new ChallengeCreateRequestDto(
			1,
			"",
			List.of("아침 세안")
		);
	}

	public static ChallengeCreateRequestDto createRequestWithEmptyRoutineNames() {
		return new ChallengeCreateRequestDto(
			1,
			"7일 챌린지",
			List.of()
		);
	}


	/**
	 * 특정 시작일로 Challenge 생성
	 */
	public static Challenge createChallengeWithStartDate(Long userId, LocalDate startDate) {
		return Challenge.builder()
			.userId(userId)
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title("7일 챌린지")
			.startDate(startDate)
			.build();
	}

	/**
	 * 기본 Challenge 생성 (ID 없음)
	 * 테스트 안정성을 위해 고정된 시작일(2024-01-01) 사용
	 */
	public static Challenge createDefaultChallenge(Long userId) {
		return createChallengeWithStartDate(userId, LocalDate.of(2024, 1, 1));
	}

	/**
	 * ChallengeRoutine 리스트 생성
	 * Challenge의 팩토리 메서드를 활용하여 생성
	 */
	public static List<ChallengeRoutine> createChallengeRoutines(Challenge challenge, List<String> routineNames) {
		return challenge.createChallengeRoutines(routineNames);
	}

	/**
	 * 특정 날짜에 예정된 ChallengeRoutine 생성
	 */
	public static ChallengeRoutine createRoutine(Challenge challenge, String name, LocalDate scheduledDate) {
		return ChallengeRoutine.builder()
			.challenge(challenge)
			.name(name)
			.scheduledDate(scheduledDate)
			.build();
	}

	public static ChallengeCreateResponseDto createChallengeResponse(Challenge challenge, List<ChallengeRoutine> routines) {
		List<ChallengeRoutineResponseDto> routineDtos = routines.stream()
			.map(ChallengeRoutineResponseDto::from)
			.toList();

		return new ChallengeCreateResponseDto(
			challenge.getId(),
			challenge.getTitle(),
			challenge.getTotalDays(),
			challenge.getStartDate(),
			challenge.getEndDate(),
			routines.size(),
			routineDtos
		);
	}
}
