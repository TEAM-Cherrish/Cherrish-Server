package com.sopt.cherrish.global.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture;

@TestConfiguration
public class TestClockConfig {

	@Bean
	public Clock clock() {
		// 테스트 안정성을 위해 고정된 시점 사용
		ZoneId zoneId = ZoneId.systemDefault();
		Instant fixedInstant = ChallengeTestFixture.FIXED_START_DATE
			.atStartOfDay(zoneId)
			.toInstant();
		return Clock.fixed(fixedInstant, zoneId);
	}
}
