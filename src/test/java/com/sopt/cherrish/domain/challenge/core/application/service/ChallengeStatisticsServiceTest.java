package com.sopt.cherrish.domain.challenge.core.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeStatisticsRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeStatisticsService 단위 테스트")
class ChallengeStatisticsServiceTest {

	@Mock
	private ChallengeStatisticsRepository statisticsRepository;

	@InjectMocks
	private ChallengeStatisticsService challengeStatisticsService;

	@Test
	@DisplayName("성공 - 챌린지 통계 초기화")
	void initializeStatisticsSuccess() {
		// given
		Challenge challenge = ChallengeTestFixture.createDefaultChallenge(1L);
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(
			List.of("루틴1", "루틴2", "루틴3"));

		ChallengeStatistics savedStatistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(routines.size())
			.build();

		when(statisticsRepository.save(any(ChallengeStatistics.class)))
			.thenReturn(savedStatistics);

		// when
		challengeStatisticsService.initializeStatistics(challenge, routines);

		// then
		verify(statisticsRepository).save(any(ChallengeStatistics.class));
	}

	@Test
	@DisplayName("성공 - 완료 개수 증가")
	void incrementCompletedCountSuccess() {
		// given
		Long challengeId = 1L;
		Challenge challenge = ChallengeTestFixture.createDefaultChallenge(1L);

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		when(statisticsRepository.findByChallengeId(challengeId))
			.thenReturn(Optional.of(statistics));

		// when
		challengeStatisticsService.incrementCompletedCount(challengeId);

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(1);
		verify(statisticsRepository).findByChallengeId(challengeId);
	}

	@Test
	@DisplayName("실패 - 완료 개수 증가 시 챌린지 통계를 찾을 수 없음")
	void incrementCompletedCountStatisticsNotFoundThrowsException() {
		// given
		Long challengeId = 999L;
		when(statisticsRepository.findByChallengeId(challengeId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> challengeStatisticsService.incrementCompletedCount(challengeId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND);
	}

	@Test
	@DisplayName("성공 - 완료 개수 감소")
	void decrementCompletedCountSuccess() {
		// given
		Long challengeId = 1L;
		Challenge challenge = ChallengeTestFixture.createDefaultChallenge(1L);

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		// 먼저 증가시켜서 감소할 여지를 만듦
		statistics.incrementCompletedCount();
		statistics.incrementCompletedCount();

		when(statisticsRepository.findByChallengeId(challengeId))
			.thenReturn(Optional.of(statistics));

		// when
		challengeStatisticsService.decrementCompletedCount(challengeId);

		// then
		assertThat(statistics.getCompletedCount()).isEqualTo(1);
		verify(statisticsRepository).findByChallengeId(challengeId);
	}

	@Test
	@DisplayName("실패 - 완료 개수 감소 시 챌린지 통계를 찾을 수 없음")
	void decrementCompletedCountStatisticsNotFoundThrowsException() {
		// given
		Long challengeId = 999L;
		when(statisticsRepository.findByChallengeId(challengeId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> challengeStatisticsService.decrementCompletedCount(challengeId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND);
	}

	@Test
	@DisplayName("성공 - 챌린지 통계 조회")
	void getStatisticsSuccess() {
		// given
		Long challengeId = 1L;
		Challenge challenge = ChallengeTestFixture.createDefaultChallenge(1L);

		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(21)
			.build();

		when(statisticsRepository.findByChallengeId(challengeId))
			.thenReturn(Optional.of(statistics));

		// when
		ChallengeStatistics result = challengeStatisticsService.getStatistics(challengeId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getTotalRoutineCount()).isEqualTo(21);
		assertThat(result.getCompletedCount()).isEqualTo(0);
		verify(statisticsRepository).findByChallengeId(challengeId);
	}

	@Test
	@DisplayName("실패 - 챌린지 통계 조회 시 통계를 찾을 수 없음")
	void getStatisticsStatisticsNotFoundThrowsException() {
		// given
		Long challengeId = 999L;
		when(statisticsRepository.findByChallengeId(challengeId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> challengeStatisticsService.getStatistics(challengeId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND);
	}
}
