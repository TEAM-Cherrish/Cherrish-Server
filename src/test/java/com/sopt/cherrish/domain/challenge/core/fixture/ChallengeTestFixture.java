package com.sopt.cherrish.domain.challenge.core.fixture;

import java.time.LocalDate;
import java.util.List;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateItemRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.RoutineUpdateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.CustomRoutineAddResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineBatchUpdateResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.RoutineCompletionResponseDto;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.global.config.TestClockConfig;

public class ChallengeTestFixture {

	// 공통 테스트 상수
	// TestClockConfig의 고정 날짜를 참조하여 일관성 유지
	public static final LocalDate FIXED_START_DATE = TestClockConfig.FIXED_TEST_DATE;
	public static final Long DEFAULT_USER_ID = 1L;
	public static final Long DEFAULT_CHALLENGE_ID = 1L;
	public static final Long DEFAULT_ROUTINE_ID = 1L;
	public static final String DEFAULT_CHALLENGE_TITLE = "피부 보습 관리";
	public static final String DEFAULT_ROUTINE_NAME = "아침 세안";
	public static final int DEFAULT_TOTAL_DAYS = 7;

	private ChallengeTestFixture() {
		// Utility class
	}

	// ===== Message Generation Helpers (matches production DTO logic) =====

	/**
	 * 루틴 완료 메시지 생성 (RoutineCompletionResponseDto.from() 로직과 동일)
	 */
	private static String createRoutineCompletionMessage(boolean isComplete) {
		return isComplete ? "루틴을 완료했습니다!" : "루틴 완료를 취소했습니다.";
	}

	/**
	 * 루틴 일괄 업데이트 메시지 생성 (RoutineBatchUpdateResponseDto.from() 로직과 동일)
	 */
	private static String createRoutineBatchUpdateMessage(int count) {
		return count + "개의 루틴이 업데이트되었습니다.";
	}

	public static ChallengeCreateRequestDto createValidChallengeRequest() {
		return new ChallengeCreateRequestDto(
			1,
			List.of("아침 세안", "토너 바르기", "크림 바르기")
		);
	}

	public static ChallengeCreateRequestDto createRequestWithEmptyRoutines() {
		return new ChallengeCreateRequestDto(
			1,
			List.of()
		);
	}

	public static ChallengeCreateRequestDto createRequestWithTooManyRoutines() {
		return new ChallengeCreateRequestDto(
			1,
			List.of("루틴1", "루틴2", "루틴3", "루틴4", "루틴5", "루틴6", "루틴7", "루틴8", "루틴9", "루틴10", "루틴11")
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
	 * 컨트롤러 테스트용: 도메인 객체 없이 Response 직접 생성
	 */
	public static ChallengeCreateResponseDto createMockChallengeCreateResponse() {
		LocalDate startDate = FIXED_START_DATE;
		List<ChallengeRoutineResponseDto> routines = List.of(
			new ChallengeRoutineResponseDto(1L, "아침 세안", startDate, false),
			new ChallengeRoutineResponseDto(2L, "토너 바르기", startDate, false),
			new ChallengeRoutineResponseDto(3L, "크림 바르기", startDate, false),
			new ChallengeRoutineResponseDto(4L, "아침 세안", startDate.plusDays(1), false),
			new ChallengeRoutineResponseDto(5L, "토너 바르기", startDate.plusDays(1), false),
			new ChallengeRoutineResponseDto(6L, "크림 바르기", startDate.plusDays(1), false)
		);

		return new ChallengeCreateResponseDto(
			DEFAULT_CHALLENGE_ID,
			DEFAULT_CHALLENGE_TITLE,
			DEFAULT_TOTAL_DAYS,
			startDate,
			startDate.plusDays(DEFAULT_TOTAL_DAYS - 1),
			21,
			routines
		);
	}

	public static ChallengeDetailResponseDto createMockChallengeDetailResponse() {
		List<ChallengeRoutineResponseDto> todayRoutines = List.of(
			new ChallengeRoutineResponseDto(1L, "아침 세안", FIXED_START_DATE, false),
			new ChallengeRoutineResponseDto(2L, "토너 바르기", FIXED_START_DATE, true),
			new ChallengeRoutineResponseDto(3L, "크림 바르기", FIXED_START_DATE, false)
		);

		return new ChallengeDetailResponseDto(
			DEFAULT_CHALLENGE_ID,
			"7일 보습 챌린지",
			3,
			42.5,
			2,
			"꽃핀 체리",
			50.0,
			3,
			todayRoutines,
			"3일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!"
		);
	}

	public static RoutineCompletionResponseDto createMockRoutineCompletionResponse(boolean isComplete) {
		return new RoutineCompletionResponseDto(
			DEFAULT_ROUTINE_ID,
			DEFAULT_ROUTINE_NAME,
			isComplete
		);
	}

	// ===== Batch Update Request Fixtures =====

	/**
	 * 정상 루틴 일괄 업데이트 요청 (3개 루틴)
	 */
	public static RoutineUpdateRequestDto createValidRoutineUpdateRequest() {
		return new RoutineUpdateRequestDto(
			List.of(
				new RoutineUpdateItemRequestDto(1L, true),
				new RoutineUpdateItemRequestDto(2L, false),
				new RoutineUpdateItemRequestDto(3L, true)
			)
		);
	}

	/**
	 * 단일 루틴 업데이트 요청
	 */
	public static RoutineUpdateRequestDto createSingleRoutineUpdateRequest() {
		return new RoutineUpdateRequestDto(
			List.of(new RoutineUpdateItemRequestDto(1L, true))
		);
	}

	/**
	 * 빈 루틴 목록 요청 - @NotEmpty 검증 실패
	 */
	public static RoutineUpdateRequestDto createRoutineUpdateRequestWithEmptyList() {
		return new RoutineUpdateRequestDto(List.of());
	}

	/**
	 * null routineId 요청 - @NotNull 검증 실패
	 */
	public static RoutineUpdateRequestDto createRoutineUpdateRequestWithNullRoutineId() {
		return new RoutineUpdateRequestDto(
			List.of(new RoutineUpdateItemRequestDto(null, true))
		);
	}

	/**
	 * null isComplete 요청 - @NotNull 검증 실패
	 */
	public static RoutineUpdateRequestDto createRoutineUpdateRequestWithNullIsComplete() {
		return new RoutineUpdateRequestDto(
			List.of(new RoutineUpdateItemRequestDto(1L, null))
		);
	}

	// ===== Batch Update Response Fixtures =====

	/**
	 * Mock 루틴 일괄 업데이트 응답 (3개 루틴)
	 */
	public static RoutineBatchUpdateResponseDto createMockRoutineBatchUpdateResponse() {
		List<ChallengeRoutineResponseDto> routines = List.of(
			new ChallengeRoutineResponseDto(1L, "아침 세안", FIXED_START_DATE, true),
			new ChallengeRoutineResponseDto(2L, "토너 바르기", FIXED_START_DATE, false),
			new ChallengeRoutineResponseDto(3L, "크림 바르기", FIXED_START_DATE, true)
		);

		return new RoutineBatchUpdateResponseDto(routines, 3);
	}

	/**
	 * Mock 단일 루틴 업데이트 응답
	 */
	public static RoutineBatchUpdateResponseDto createMockSingleRoutineBatchUpdateResponse() {
		List<ChallengeRoutineResponseDto> routines = List.of(
			new ChallengeRoutineResponseDto(1L, "아침 세안", FIXED_START_DATE, true)
		);

		return new RoutineBatchUpdateResponseDto(routines, 1);
	}

	/**
	 * Mock 커스텀 루틴 추가 응답
	 */
	public static CustomRoutineAddResponseDto createMockCustomRoutineAddResponse() {

		// 테스트용 총 루틴 수: 기존 21개 + 커스텀 루틴 5개 (테스트 시나리오 가정)
		int mockTotalRoutineCount = 26;

		Challenge challenge = Challenge.builder()
			.userId(DEFAULT_USER_ID)
			.homecareRoutine(HomecareRoutine.SKIN_MOISTURIZING)
			.title("테스트 챌린지")
			.startDate(FIXED_START_DATE)
			.build();

		return CustomRoutineAddResponseDto.from(
			challenge,
			"저녁 마사지",
			List.of(), // Mock 응답이므로 생성된 루틴 리스트는 빈 값으로 처리
			mockTotalRoutineCount
		);
	}
}
