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
	 * ID가 설정된 Challenge 생성 (Mock 테스트용)
	 * 참고: 실제 ID 필드는 private이므로 이 메서드로는 설정할 수 없습니다.
	 * Mock 객체를 사용하거나, 통합 테스트에서 실제 DB 저장 후 조회하세요.
	 *
	 * @deprecated Use mock objects with when().thenReturn() or integration tests
	 */
	@Deprecated
	public static Challenge createChallenge(Long challengeId, Long userId) {
		return createChallengeWithStartDate(userId, LocalDate.of(2024, 1, 1));
	}

	/**
	 * Mock 테스트용: ID를 포함한 Challenge Mock 응답 생성
	 * Mockito의 when().thenReturn()과 함께 사용
	 */
	public static Challenge createMockChallengeWithId(Long challengeId, Long userId, LocalDate startDate) {

		// Mock을 사용하는 테스트에서는 when().thenReturn()으로 이 객체를 반환하되,
		// getId()를 호출할 때 challengeId를 반환하도록 설정해야 합니다.
		// 또는 통합 테스트에서 실제 저장된 객체를 사용하세요.
		return Challenge.builder()
			.userId(userId)
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title("7일 챌린지")
			.startDate(startDate)
			.build();
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
