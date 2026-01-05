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

	public static Challenge createChallenge(Long challengeId, Long userId) {
		Challenge challenge = Challenge.builder()
			.userId(userId)
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title("7일 챌린지")
			.startDate(LocalDate.now())
			.build();

		// Reflection을 통한 ID 설정 (테스트용)
		try {
			var idField = Challenge.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(challenge, challengeId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return challenge;
	}

	public static List<ChallengeRoutine> createChallengeRoutines(Challenge challenge, List<String> routineNames) {
		List<ChallengeRoutine> routines = new ArrayList<>();
		long routineId = 1L;

		for (int day = 0; day < 7; day++) {
			LocalDate scheduledDate = challenge.getStartDate().plusDays(day);

			for (String routineName : routineNames) {
				ChallengeRoutine routine = ChallengeRoutine.builder()
					.challenge(challenge)
					.name(routineName)
					.scheduledDate(scheduledDate)
					.build();

				// Reflection을 통한 ID 설정 (테스트용)
				try {
					var idField = ChallengeRoutine.class.getDeclaredField("id");
					idField.setAccessible(true);
					idField.set(routine, routineId++);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				routines.add(routine);
			}
		}

		return routines;
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
