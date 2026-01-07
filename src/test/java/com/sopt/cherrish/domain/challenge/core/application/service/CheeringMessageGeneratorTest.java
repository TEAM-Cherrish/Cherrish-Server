package com.sopt.cherrish.domain.challenge.core.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("CheeringMessageGenerator 단위 테스트")
class CheeringMessageGeneratorTest {

	private final CheeringMessageGenerator generator = new CheeringMessageGenerator();
	private static final int TOTAL_DAYS = 7;

	@Test
	@DisplayName("챌린지 시작 전 - 준비 메시지 반환 (currentDay <= 0)")
	void generateBeforeStartReturnsPreparationMessage() {
		// when
		String message = generator.generate(0, TOTAL_DAYS);

		// then
		assertThat(message).isEqualTo("챌린지가 곧 시작됩니다. 준비하세요!");
	}

	@Test
	@DisplayName("챌린지 시작 전 - 음수 일차도 준비 메시지 반환")
	void generateNegativeDayReturnsPreparationMessage() {
		// when
		String message = generator.generate(-1, TOTAL_DAYS);

		// then
		assertThat(message).isEqualTo("챌린지가 곧 시작됩니다. 준비하세요!");
	}

	@Test
	@DisplayName("첫째 날 - 시작 메시지 반환 (currentDay = 1)")
	void generateFirstDayReturnsStartMessage() {
		// when
		String message = generator.generate(1, TOTAL_DAYS);

		// then
		assertThat(message).isEqualTo("챌린지 시작! 오늘부터 피부를 위한 첫 걸음입니다.");
	}

	@Test
	@DisplayName("중간 지점 - 절반 달성 메시지 반환 (currentDay = totalDays / 2)")
	void generateHalfwayPointReturnsHalfwayMessage() {
		// when
		String message = generator.generate(3, 6); // 6일 챌린지의 절반

		// then
		assertThat(message).isEqualTo("절반을 달성했어요! 끝까지 함께 해봐요!");
	}

	@Test
	@DisplayName("마지막 날 - 완주 응원 메시지 반환 (currentDay = totalDays)")
	void generateLastDayReturnsLastDayMessage() {
		// when
		String message = generator.generate(7, TOTAL_DAYS);

		// then
		assertThat(message).isEqualTo("마지막 날입니다! 완주까지 조금만 더 힘내세요!");
	}

	@Test
	@DisplayName("마지막 날 이후 - 완주 메시지 유지 (currentDay > totalDays)")
	void generateAfterLastDayReturnsLastDayMessage() {
		// when
		String message = generator.generate(8, TOTAL_DAYS);

		// then
		assertThat(message).isEqualTo("마지막 날입니다! 완주까지 조금만 더 힘내세요!");
	}

	@ParameterizedTest
	@CsvSource({
		"2, 7, 2일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!",
		"4, 7, 4일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!",
		"5, 7, 5일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!",
		"6, 7, 6일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!",
		"2, 9, 2일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!"
	})
	@DisplayName("일반적인 경우 - 일차별 기본 응원 메시지")
	void generateNormalDaysReturnsNormalMessage(int currentDay, int totalDays, String expectedMessage) {
		// when
		String message = generator.generate(currentDay, totalDays);

		// then
		assertThat(message).isEqualTo(expectedMessage);
	}

	@Test
	@DisplayName("우선순위 확인 - 중간 지점이 마지막 날과 겹치는 경우 마지막 날 우선")
	void generateHalfwayAndLastDayConflict() {
		// given - 2일 챌린지의 경우 2일차가 중간(1일)이면서 마지막
		int currentDay = 2;
		int totalDays = 2;

		// when
		String message = generator.generate(currentDay, totalDays);

		// then - 마지막 날 메시지가 우선순위 높음
		assertThat(message).isEqualTo("마지막 날입니다! 완주까지 조금만 더 힘내세요!");
	}

	@ParameterizedTest
	@CsvSource({
		"7, 14",  // 14일 챌린지
		"15, 30", // 30일 챌린지
		"3, 6",   // 6일 챌린지
		"3, 7"    // 7일 챌린지 (정수 나눗셈)
	})
	@DisplayName("다양한 챌린지 길이의 중간 지점 메시지")
	void generateVariousChallengeLengthsHalfwayPoint(int currentDay, int totalDays) {
		// when
		String message = generator.generate(currentDay, totalDays);

		// then
		assertThat(message).isEqualTo("절반을 달성했어요! 끝까지 함께 해봐요!");
	}

	@Test
	@DisplayName("예외 - totalDays가 0인 경우")
	void generateWithZeroTotalDaysThrowsException() {
		// when & then
		assertThatThrownBy(() -> generator.generate(1, 0))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("totalDays는 0보다 커야 합니다");
	}

	@Test
	@DisplayName("예외 - totalDays가 음수인 경우")
	void generateWithNegativeTotalDaysThrowsException() {
		// when & then
		assertThatThrownBy(() -> generator.generate(1, -7))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("totalDays는 0보다 커야 합니다");
	}

	@Test
	@DisplayName("1일 챌린지 - 첫째 날과 마지막 날이 겹치는 경우")
	void generateOneDayChallenge() {
		// when
		String message = generator.generate(1, 1);

		// then - 첫째 날 메시지가 우선순위 높음
		assertThat(message).isEqualTo("챌린지 시작! 오늘부터 피부를 위한 첫 걸음입니다.");
	}

	@Test
	@DisplayName("1일 챌린지 - 마지막 날 이후")
	void generateOneDayChallengeAfterEnd() {
		// when
		String message = generator.generate(2, 1);

		// then - 마지막 날 메시지
		assertThat(message).isEqualTo("마지막 날입니다! 완주까지 조금만 더 힘내세요!");
	}

	@Test
	@DisplayName("매우 큰 값에 대한 처리 (100일 챌린지)")
	void generateWithLargeValues() {
		// when
		String message = generator.generate(50, 100);

		// then - 정상적으로 중간 지점 메시지 반환
		assertThat(message).isEqualTo("절반을 달성했어요! 끝까지 함께 해봐요!");
	}

	@Test
	@DisplayName("매우 큰 값 - 일반적인 경우")
	void generateWithLargeValuesNormalDay() {
		// when
		String message = generator.generate(30, 100);

		// then - 정상적으로 일반 메시지 반환
		assertThat(message).isEqualTo("30일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!");
	}
}
