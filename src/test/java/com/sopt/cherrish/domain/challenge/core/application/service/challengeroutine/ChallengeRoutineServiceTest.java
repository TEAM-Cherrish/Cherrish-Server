package com.sopt.cherrish.domain.challenge.core.application.service.challengeroutine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeRoutineService 단위 테스트")
class ChallengeRoutineServiceTest {

	@Mock
	private ChallengeRoutineRepository routineRepository;

	@Mock
	private Clock clock;

	@InjectMocks
	private ChallengeRoutineService challengeRoutineService;

	@Test
	@DisplayName("성공 - 챌린지 루틴 생성 및 Batch Insert")
	void createAndSaveRoutinesSuccess() {
		// given
		Challenge challenge = ChallengeTestFixture.createDefaultChallenge(1L);
		List<String> routineNames = List.of("아침 세안", "토너 바르기", "크림 바르기");

		List<ChallengeRoutine> expectedRoutines = challenge.createChallengeRoutines(routineNames);

		when(routineRepository.saveAll(anyList()))
			.thenReturn(expectedRoutines);

		// when
		List<ChallengeRoutine> result = challengeRoutineService.createAndSaveRoutines(
			challenge, routineNames);

		// then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(21); // 3 routines × 7 days
		verify(routineRepository).saveAll(anyList());
	}

	@Test
	@DisplayName("성공 - 오늘의 루틴 조회")
	void getTodayRoutinesSuccess() {
		// given
		Long challengeId = 1L;
		Challenge challenge = ChallengeTestFixture.createDefaultChallenge(1L);
		LocalDate today = challenge.getStartDate();

		// Clock Mock 설정 - fixture의 시작 날짜로 고정
		Clock fixedClock = Clock.fixed(
			today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
			ZoneId.systemDefault()
		);
		when(clock.instant()).thenReturn(fixedClock.instant());
		when(clock.getZone()).thenReturn(fixedClock.getZone());

		List<ChallengeRoutine> expectedRoutines = challenge.createChallengeRoutines(
			List.of("루틴1", "루틴2")).stream()
			.filter(routine -> routine.getScheduledDate().equals(today))
			.toList();

		when(routineRepository.findByChallengeIdAndScheduledDate(challengeId, today))
			.thenReturn(expectedRoutines);

		// when
		List<ChallengeRoutine> result = challengeRoutineService.getTodayRoutines(challengeId);

		// then
		assertThat(result).isNotNull();
		assertThat(result).allMatch(routine -> routine.getScheduledDate().equals(today));
		verify(routineRepository).findByChallengeIdAndScheduledDate(challengeId, today);
	}

	@Test
	@DisplayName("성공 - 특정 날짜의 루틴 조회")
	void getRoutinesByDateSuccess() {
		// given
		Long challengeId = 1L;
		Challenge challenge = ChallengeTestFixture.createDefaultChallenge(1L);
		LocalDate scheduledDate = challenge.getStartDate().plusDays(3);

		List<ChallengeRoutine> expectedRoutines = challenge.createChallengeRoutines(
			List.of("루틴1", "루틴2")).stream()
			.filter(routine -> routine.getScheduledDate().equals(scheduledDate))
			.toList();

		when(routineRepository.findByChallengeIdAndScheduledDate(challengeId, scheduledDate))
			.thenReturn(expectedRoutines);

		// when
		List<ChallengeRoutine> result = challengeRoutineService.getRoutinesByDate(
			challengeId, scheduledDate);

		// then
		assertThat(result).isNotNull();
		assertThat(result).allMatch(routine -> routine.getScheduledDate().equals(scheduledDate));
		verify(routineRepository).findByChallengeIdAndScheduledDate(challengeId, scheduledDate);
	}

	@Test
	@DisplayName("성공 - 특정 날짜에 루틴이 없을 때 빈 리스트 반환")
	void getRoutinesByDateNoRoutinesReturnsEmptyList() {
		// given
		Long challengeId = 1L;
		LocalDate scheduledDate = LocalDate.now().plusDays(10);

		when(routineRepository.findByChallengeIdAndScheduledDate(challengeId, scheduledDate))
			.thenReturn(List.of());

		// when
		List<ChallengeRoutine> result = challengeRoutineService.getRoutinesByDate(
			challengeId, scheduledDate);

		// then
		assertThat(result).isEmpty();
		verify(routineRepository).findByChallengeIdAndScheduledDate(challengeId, scheduledDate);
	}
}
