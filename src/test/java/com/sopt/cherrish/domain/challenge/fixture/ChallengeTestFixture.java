package com.sopt.cherrish.domain.challenge.fixture;

import java.util.List;

import com.sopt.cherrish.domain.challenge.application.dto.response.HomecareRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.presentation.dto.request.AiRecommendationRequestDto;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.AiRecommendationResponseDto;

public class ChallengeTestFixture {

	// HomecareRoutineResponseDto Fixtures
	public static HomecareRoutineResponseDto skinMoisturizingRoutine() {
		return new HomecareRoutineResponseDto(1, "SKIN_MOISTURIZING", "피부 보습 관리");
	}

	public static HomecareRoutineResponseDto skinBrighteningRoutine() {
		return new HomecareRoutineResponseDto(2, "SKIN_BRIGHTENING", "피부 미백 관리");
	}

	public static HomecareRoutineResponseDto wrinkleCareRoutine() {
		return new HomecareRoutineResponseDto(3, "WRINKLE_CARE", "주름 개선 관리");
	}

	public static HomecareRoutineResponseDto troubleCareRoutine() {
		return new HomecareRoutineResponseDto(4, "TROUBLE_CARE", "트러블 케어");
	}

	public static HomecareRoutineResponseDto poreCareRoutine() {
		return new HomecareRoutineResponseDto(5, "PORE_CARE", "모공 관리");
	}

	public static HomecareRoutineResponseDto elasticityCareRoutine() {
		return new HomecareRoutineResponseDto(6, "ELASTICITY_CARE", "탄력 관리");
	}

	public static List<HomecareRoutineResponseDto> homecareRoutineList() {
		return List.of(
			skinMoisturizingRoutine(),
			skinBrighteningRoutine(),
			wrinkleCareRoutine(),
			troubleCareRoutine(),
			poreCareRoutine(),
			elasticityCareRoutine()
		);
	}

	// AiRecommendationRequestDto Fixtures
	public static AiRecommendationRequestDto aiRecommendationRequest(Long homecareRoutineId) {
		return new AiRecommendationRequestDto(homecareRoutineId);
	}

	public static AiRecommendationRequestDto skinMoisturizingRequest() {
		return aiRecommendationRequest(1L);
	}

	public static AiRecommendationRequestDto wrinkleCareRequest() {
		return aiRecommendationRequest(3L);
	}

	// AiRecommendationResponseDto Fixtures
	public static AiRecommendationResponseDto skinMoisturizingRecommendation() {
		return AiRecommendationResponseDto.of(
			"피부 보습 7일 챌린지",
			List.of("아침 세안 후 토너 바르기", "저녁 보습 크림 바르기", "하루 8잔 물 마시기")
		);
	}

	public static AiRecommendationResponseDto wrinkleCareRecommendation() {
		return AiRecommendationResponseDto.of(
			"주름 개선 7일 챌린지",
			List.of("레티놀 세럼 바르기", "충분한 수면")
		);
	}

	public static AiRecommendationResponseDto emptyRoutinesRecommendation() {
		return AiRecommendationResponseDto.of("테스트 챌린지", List.of());
	}

	public static AiRecommendationResponseDto customRecommendation(String title, List<String> routines) {
		return AiRecommendationResponseDto.of(title, routines);
	}
}
