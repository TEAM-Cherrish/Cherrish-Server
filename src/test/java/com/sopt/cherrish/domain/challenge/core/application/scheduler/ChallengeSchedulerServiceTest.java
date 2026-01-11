package com.sopt.cherrish.domain.challenge.core.application.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeSchedulerService 단위 테스트")
class ChallengeSchedulerServiceTest {

	private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
	private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

	@Mock
	private ChallengeRepository challengeRepository;

	@Mock
	private Clock clock;

	private ChallengeSchedulerService challengeSchedulerService;

	@BeforeEach
	void setUp() {
		// 고정된 날짜의 Clock 설정
		Clock fixedClock = Clock.fixed(
			FIXED_DATE.atStartOfDay(ZONE_ID).toInstant(),
			ZONE_ID
		);
		when(clock.instant()).thenReturn(fixedClock.instant());
		when(clock.getZone()).thenReturn(fixedClock.getZone());

		challengeSchedulerService = new ChallengeSchedulerService(challengeRepository, clock);
	}

	@Test
	@DisplayName("만료된 챌린지 벌크 업데이트 실행 성공")
	void expireCompletedChallengesSuccess() {
		// Given: 3개의 챌린지가 업데이트될 것으로 예상
		when(challengeRepository.bulkUpdateExpiredChallenges(any(LocalDate.class)))
			.thenReturn(3);

		// When: 스케줄러 메서드 실행
		challengeSchedulerService.expireCompletedChallenges();

		// Then: bulkUpdateExpiredChallenges가 고정된 날짜로 1번 호출됨
		ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
		verify(challengeRepository, times(1)).bulkUpdateExpiredChallenges(dateCaptor.capture());

		// 호출된 날짜가 고정된 날짜인지 검증 (시간에 의존하지 않는 결정론적 테스트)
		LocalDate capturedDate = dateCaptor.getValue();
		assertThat(capturedDate).isEqualTo(FIXED_DATE);
	}

	@Test
	@DisplayName("만료된 챌린지가 없을 때 정상 동작")
	void expireCompletedChallengesNoExpiredChallenges() {
		// Given: 만료된 챌린지 없음
		when(challengeRepository.bulkUpdateExpiredChallenges(any(LocalDate.class)))
			.thenReturn(0);

		// When
		challengeSchedulerService.expireCompletedChallenges();

		// Then: 메서드 호출은 정상적으로 완료
		verify(challengeRepository, times(1)).bulkUpdateExpiredChallenges(any(LocalDate.class));
	}

	@Test
	@DisplayName("많은 수의 챌린지 업데이트 처리")
	void expireCompletedChallengesLargeNumber() {
		// Given: 100개의 챌린지가 업데이트될 것으로 예상
		when(challengeRepository.bulkUpdateExpiredChallenges(any(LocalDate.class)))
			.thenReturn(100);

		// When
		challengeSchedulerService.expireCompletedChallenges();

		// Then: 단일 벌크 업데이트로 처리됨
		verify(challengeRepository, times(1)).bulkUpdateExpiredChallenges(any(LocalDate.class));
	}
}
