package com.sopt.cherrish.domain.challenge.core.application.service;

import static com.sopt.cherrish.domain.challenge.core.application.service.CheeringMessageGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CheeringMessageGenerator 단위 테스트")
class CheeringMessageGeneratorTest {

	private final CheeringMessageGenerator generator = new CheeringMessageGenerator();
	private static final int TOTAL_DAYS = 7;

	@ParameterizedTest
	@ValueSource(ints = {-5, -1, 0})
	@DisplayName("챌린지 시작 전 - 준비 메시지 반환")
	void generateBeforeStartReturnsPreparationMessage(int currentDay) {
		// when
		String message = generator.generate(currentDay, TOTAL_DAYS);

		// then
		assertThat(message).isEqualTo(PREPARATION_MESSAGE);
	}

	@Test
	@DisplayName("첫째 날 - 시작 메시지 반환 (currentDay = 1)")
	void generateFirstDayReturnsStartMessage() {
		// when
		String message = generator.generate(1, TOTAL_DAYS);

		// then
		assertThat(message).isEqualTo(FIRST_DAY_MESSAGE);
	}

	@Test
	@DisplayName("중간 지점 - 절반 달성 메시지 반환 (currentDay = totalDays / 2)")
	void generateHalfwayPointReturnsHalfwayMessage() {
		// when
		String message = generator.generate(3, 6); // 6일 챌린지의 절반

		// then
		assertThat(message).isEqualTo(HALFWAY_MESSAGE);
	}

	@ParameterizedTest
	@ValueSource(ints = {7, 8, 10})
	@DisplayName("마지막 날 및 이후 - 완주 응원 메시지 반환")
	void generateLastDayReturnsLastDayMessage(int currentDay) {
		// when
		String message = generator.generate(currentDay, TOTAL_DAYS);

		// then
		assertThat(message).isEqualTo(LAST_DAY_MESSAGE);
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
		assertThat(message).isEqualTo(LAST_DAY_MESSAGE);
	}

	@ParameterizedTest
	@CsvSource({
		"7, 14",   // 14일의 절반
		"15, 30",  // 30일의 절반
		"3, 6",    // 6일의 절반
		"3, 7",    // 7일의 절반 (7/2=3, 내림)
		"5, 11"    // 11일의 절반 (11/2=5, 내림)
	})
	@DisplayName("다양한 챌린지 길이의 중간 지점 메시지 (정수 나눗셈 확인)")
	void generateVariousChallengeLengthsHalfwayPoint(int currentDay, int totalDays) {
		// when
		String message = generator.generate(currentDay, totalDays);

		// then
		assertThat(message).isEqualTo(HALFWAY_MESSAGE);
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -7, -100})
	@DisplayName("예외 - totalDays가 0 이하인 경우")
	void generateWithInvalidTotalDaysThrowsException(int invalidTotalDays) {
		// when & then
		assertThatThrownBy(() -> generator.generate(1, invalidTotalDays))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("totalDays는 0보다 커야 합니다");
	}

	@ParameterizedTest
	@CsvSource({
		"1, 1",   // 1일 챌린지 - 첫째 날이 우선
		"50, 100", // 100일 챌린지 중간 지점
		"30, 100"  // 100일 챌린지 일반 날짜
	})
	@DisplayName("매우 긴 챌린지 기간에 대한 정상 동작 확인")
	void generateWithLargeValues(int currentDay, int totalDays) {
		// when
		String message = generator.generate(currentDay, totalDays);

		// then - 메시지가 정상적으로 생성됨
		assertThat(message).isNotNull().isNotEmpty();
	}

	@Test
	@DisplayName("메시지 의미 검증 - 모든 메시지는 null이 아니고 비어있지 않음")
	void allMessagesAreNotNullOrEmpty() {
		// when & then - 다양한 시나리오에서 메시지 생성
		for (int day = -1; day <= 10; day++) {
			String message = generator.generate(day, 7);
			assertThat(message)
				.as("Day %d should have a valid message", day)
				.isNotNull()
				.isNotEmpty();
		}
	}

	@Test
	@DisplayName("메시지 의미 검증 - 마지막 날 메시지는 긍정적인 완주 동기 부여 포함")
	void lastDayMessageContainsMotivation() {
		// when
		String message = generator.generate(7, 7);

		// then
		assertThat(message)
			.contains("마지막")
			.containsAnyOf("완주", "힘내");
	}

	@Test
	@DisplayName("전체 챌린지 기간 동안 메시지 변화 확인")
	void messageProgressionThroughoutChallenge() {
		// when - 7일 챌린지 전체 기간의 메시지 수집
		java.util.List<String> messages = new java.util.ArrayList<>();
		for (int day = 1; day <= 7; day++) {
			messages.add(generator.generate(day, 7));
		}

		// then
		// 첫 날과 마지막 날 메시지가 다른지 확인
		assertThat(messages.get(0)).isNotEqualTo(messages.get(6));

		// 중복되지 않은 메시지가 여러 개 존재하는지 확인
		long distinctMessages = messages.stream().distinct().count();
		assertThat(distinctMessages).isGreaterThan(1);
	}
}
