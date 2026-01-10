package com.sopt.cherrish.global.config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestClockConfig {

	/**
	 * 테스트용 고정 날짜
	 * 테스트 안정성을 위해 모든 테스트에서 동일한 기준 시점 사용
	 */
	public static final LocalDate FIXED_TEST_DATE = LocalDate.of(2024, 1, 1);

	@Bean
	public Clock clock() {
		// 테스트 안정성을 위해 고정된 시점 사용
		ZoneId zoneId = ZoneId.systemDefault();
		Instant fixedInstant = FIXED_TEST_DATE
			.atStartOfDay(zoneId)
			.toInstant();
		return Clock.fixed(fixedInstant, zoneId);
	}
}
