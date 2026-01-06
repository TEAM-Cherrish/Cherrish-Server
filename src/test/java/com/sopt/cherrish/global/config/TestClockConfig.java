package com.sopt.cherrish.global.config;

import java.time.Clock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestClockConfig {

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}
