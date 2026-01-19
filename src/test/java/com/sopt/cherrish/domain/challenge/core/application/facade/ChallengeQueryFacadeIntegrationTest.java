package com.sopt.cherrish.domain.challenge.core.application.facade;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeStatisticsService;
import com.sopt.cherrish.domain.challenge.core.application.service.CheeringMessageGenerator;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeStatisticsRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeDetailResponseDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeRoutineResponseDto;
import com.sopt.cherrish.global.config.QueryDslConfig;
import com.sopt.cherrish.global.config.TestClockConfig;
import com.sopt.cherrish.global.config.TestJpaAuditConfig;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import({
	TestJpaAuditConfig.class,
	TestClockConfig.class,
	QueryDslConfig.class,
	ChallengeQueryFacade.class,
	ChallengeService.class,
	ChallengeRoutineService.class,
	ChallengeStatisticsService.class,
	CheeringMessageGenerator.class
})
@DisplayName("ChallengeQueryFacade 통합 테스트")
class ChallengeQueryFacadeIntegrationTest {

	@Autowired
	private ChallengeQueryFacade challengeQueryFacade;

	@Autowired
	private ChallengeRepository challengeRepository;

	@Autowired
	private ChallengeRoutineRepository routineRepository;

	@Autowired
	private ChallengeStatisticsRepository statisticsRepository;

	@Autowired
	private EntityManager entityManager;

	// 테스트 상수
	private static final Long TEST_USER_ID = 1L;
	private static final int TOTAL_CHALLENGE_DAYS = 7;
	private static final int DEFAULT_ROUTINE_COUNT_PER_DAY = 3;
	private static final int TOTAL_ROUTINES_PER_WEEK = DEFAULT_ROUTINE_COUNT_PER_DAY * TOTAL_CHALLENGE_DAYS; // 21

	/**
	 * Hibernate Statistics를 이용해 쿼리 카운트를 가져옴
	 */
	private long getQueryCount() {
		SessionFactory sessionFactory = entityManager.getEntityManagerFactory()
			.unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		return statistics.getQueryExecutionCount();
	}

	/**
	 * Hibernate Statistics를 초기화
	 */
	private void clearStatistics() {
		SessionFactory sessionFactory = entityManager.getEntityManagerFactory()
			.unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		statistics.setStatisticsEnabled(true);
		statistics.clear();
	}

	@Test
	@DisplayName("성공 - 활성 챌린지 상세 조회 (첫째 날)")
	void getActiveChallengeDetailSuccessFirstDay() {
		// given
		LocalDate startDate = ChallengeTestFixture.FIXED_START_DATE; // 2024-01-01
		Challenge challenge = createAndSaveChallengeWithRoutines(TEST_USER_ID, startDate, DEFAULT_ROUTINE_COUNT_PER_DAY);

		// when - TestClockConfig의 고정 시간은 2024-01-01이므로 첫째 날
		ChallengeDetailResponseDto response = challengeQueryFacade.getActiveChallengeDetail(TEST_USER_ID);

		// then
		assertThat(response.challengeId()).isEqualTo(challenge.getId());
		assertThat(response.title()).isEqualTo(ChallengeTestFixture.DEFAULT_CHALLENGE_TITLE);
		assertThat(response.currentDay()).isEqualTo(1);
		assertThat(response.progressPercentage()).isEqualTo(0);
		assertThat(response.cherryLevel()).isEqualTo(1);
		assertThat(response.progressToNextLevel()).isEqualTo(0.0);
		assertThat(response.todayRoutines()).hasSize(3);
		assertThat(response.cheeringMessage())
			.isEqualTo(CheeringMessageGenerator.FIRST_DAY_MESSAGE);
	}

	@Test
	@DisplayName("성공 - 활성 챌린지 상세 조회 (중간 지점)")
	void getActiveChallengeDetailSuccessHalfwayPoint() {
		// given - 4일차 (7일 챌린지의 중간은 4일차는 아니지만 가정)
		LocalDate startDate = ChallengeTestFixture.FIXED_START_DATE.minusDays(3);
		createAndSaveChallengeWithRoutines(TEST_USER_ID, startDate, 1);

		// when
		ChallengeDetailResponseDto response = challengeQueryFacade.getActiveChallengeDetail(TEST_USER_ID);

		// then
		assertThat(response.currentDay()).isEqualTo(4);
		assertThat(response.cheeringMessage())
			.isNotBlank()
			.contains("4일차")
			.contains("루틴");
	}

	@Test
	@DisplayName("성공 - 활성 챌린지 상세 조회 (마지막 날)")
	void getActiveChallengeDetailSuccessLastDay() {
		// given - 7일차
		LocalDate startDate = ChallengeTestFixture.FIXED_START_DATE.minusDays(6);
		createAndSaveChallengeWithRoutines(TEST_USER_ID, startDate, 2);

		// when
		ChallengeDetailResponseDto response = challengeQueryFacade.getActiveChallengeDetail(TEST_USER_ID);

		// then
		assertThat(response.currentDay()).isEqualTo(7);
		assertThat(response.cheeringMessage())
			.isEqualTo(CheeringMessageGenerator.LAST_DAY_MESSAGE);
	}

	@Test
	@DisplayName("성공 - Fetch Join으로 N+1 문제 방지 확인")
	void getActiveChallengeDetailFetchJoinPreventsNPlusOne() {
		// given
		LocalDate startDate = ChallengeTestFixture.FIXED_START_DATE;
		createAndSaveChallengeWithRoutines(TEST_USER_ID, startDate, DEFAULT_ROUTINE_COUNT_PER_DAY);

		// 영속성 컨텍스트 초기화 - 캐시 없이 실제 쿼리 실행 확인
		flushAndClear();
		clearStatistics();

		// when
		long queryCountBefore = getQueryCount();
		ChallengeDetailResponseDto response = challengeQueryFacade.getActiveChallengeDetail(TEST_USER_ID);
		long queryCountAfter = getQueryCount();

		// then - N+1 문제가 발생하지 않도록 최소한의 쿼리만 실행
		// (Challenge+Statistics Fetch Join, Routines 조회)
		long executedQueries = queryCountAfter - queryCountBefore;
		assertThat(executedQueries)
			.as("N+1 문제 방지: 루틴 개수(%d)에 비해 쿼리가 많지 않아야 함", DEFAULT_ROUTINE_COUNT_PER_DAY)
			.isLessThanOrEqualTo(3);

		// 통계 정보가 추가 쿼리 없이 조회됨
		assertThat(response.progressPercentage()).isNotNull();
		assertThat(response.cherryLevel()).isNotNull();
		assertThat(response.progressToNextLevel()).isNotNull();
	}

	@Test
	@DisplayName("성공 - 오늘의 루틴만 조회됨 (다른 날짜 루틴 제외)")
	void getActiveChallengeDetailOnlyTodayRoutines() {
		// given
		LocalDate startDate = ChallengeTestFixture.FIXED_START_DATE.minusDays(1);
		createAndSaveChallengeWithRoutines(TEST_USER_ID, startDate, DEFAULT_ROUTINE_COUNT_PER_DAY);

		// when
		ChallengeDetailResponseDto response = challengeQueryFacade.getActiveChallengeDetail(TEST_USER_ID);

		// then - 전체 루틴은 21개지만 오늘 것만 3개 반환
		assertThat(routineRepository.count()).isEqualTo(TOTAL_ROUTINES_PER_WEEK);
		assertThat(response.todayRoutines()).hasSize(DEFAULT_ROUTINE_COUNT_PER_DAY);
	}

	@Test
	@DisplayName("실패 - 활성 챌린지가 없을 때")
	void getActiveChallengeDetailNoActiveChallengeThrowsException() {
		// given - 챌린지 없음

		// when & then
		assertThatThrownBy(() -> challengeQueryFacade.getActiveChallengeDetail(TEST_USER_ID))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND);
	}

	@Test
	@DisplayName("실패 - 다른 사용자의 챌린지는 조회되지 않음")
	void getActiveChallengeDetailOtherUserChallengeNotFound() {
		// given
		Long otherUserId = 999L;
		createAndSaveChallengeWithRoutines(otherUserId, ChallengeTestFixture.FIXED_START_DATE, 2);

		// when & then - TEST_USER_ID로는 조회 안됨
		assertThatThrownBy(() -> challengeQueryFacade.getActiveChallengeDetail(TEST_USER_ID))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND);
	}

	@Test
	@DisplayName("성공 - 완료된 루틴과 미완료 루틴 구분")
	void getActiveChallengeDetailDistinguishesCompletedRoutines() {
		// given
		LocalDate startDate = ChallengeTestFixture.FIXED_START_DATE;
		Challenge challenge = createAndSaveChallengeWithRoutines(TEST_USER_ID, startDate, DEFAULT_ROUTINE_COUNT_PER_DAY);

		// 첫 번째 루틴만 완료
		completeTodayRoutine(challenge);

		// when
		ChallengeDetailResponseDto response = challengeQueryFacade.getActiveChallengeDetail(TEST_USER_ID);

		// then
		assertThat(response.todayRoutines()).hasSize(3);
		long completedCount = response.todayRoutines().stream()
			.filter(ChallengeRoutineResponseDto::isComplete)
			.count();
		assertThat(completedCount).isEqualTo(1);
	}

	@ParameterizedTest
	@CsvSource({
		"5, 1, 24",   // 21개 중 5개 완료 (23.8% -> 24%) - 레벨 1
		"8, 2, 38",   // 21개 중 8개 완료 (38.1% -> 38%) - 레벨 2
		"13, 3, 62",  // 21개 중 13개 완료 (61.9% -> 62%) - 레벨 3
		"16, 4, 76"   // 21개 중 16개 완료 (76.2% -> 76%) - 레벨 4
	})
	@DisplayName("성공 - 체리 레벨별 진행도 계산 정확성")
	void getActiveChallengeDetailCherryLevels(
		int completeCount,
		int expectedLevel,
		int expectedProgress
	) {
		// given
		LocalDate startDate = ChallengeTestFixture.FIXED_START_DATE;
		Challenge challenge = createAndSaveChallengeWithRoutines(TEST_USER_ID, startDate, DEFAULT_ROUTINE_COUNT_PER_DAY);

		// 지정된 개수만큼 루틴 완료
		completeRoutines(challenge, completeCount);

		// when
		ChallengeDetailResponseDto response = challengeQueryFacade.getActiveChallengeDetail(TEST_USER_ID);

		// then
		assertThat(response.cherryLevel()).isEqualTo(expectedLevel);
		assertThat(response.progressPercentage()).isEqualTo(expectedProgress);

		// 레벨 4가 아닌 경우 다음 레벨까지 진행도 검증
		if (expectedLevel < 4) {
			assertThat(response.progressToNextLevel()).isGreaterThan(0.0).isLessThan(100.0);
		}
	}

	/**
	 * 챌린지와 루틴을 생성하고 저장하는 헬퍼 메서드
	 * @param userId 사용자 ID
	 * @param startDate 시작일
	 * @param routineCountPerDay 하루당 루틴 개수
	 * @return 저장된 챌린지
	 */
	private Challenge createAndSaveChallengeWithRoutines(Long userId, LocalDate startDate, int routineCountPerDay) {
		Challenge challenge = Challenge.builder()
			.userId(userId)
			.homecareRoutine(DEFAULT_HOMECARE_ROUTINE)
			.title(ChallengeTestFixture.DEFAULT_CHALLENGE_TITLE)
			.startDate(startDate)
			.build();

		Challenge savedChallenge = challengeRepository.save(challenge);

		// 통계 생성
		int totalRoutineCount = routineCountPerDay * 7;
		ChallengeStatistics statistics = ChallengeStatistics.builder()
			.challenge(savedChallenge)
			.totalRoutineCount(totalRoutineCount)
			.build();
		statisticsRepository.save(statistics);

		// 루틴 생성 (7일치)
		List<String> routineNames = List.of("루틴1", "루틴2", "루틴3").subList(0, routineCountPerDay);
		List<ChallengeRoutine> routines = savedChallenge.createChallengeRoutines(routineNames);
		routineRepository.saveAll(routines);

		// 영속성 컨텍스트를 DB에 반영하고 초기화
		flushAndClear();

		// 통계가 포함된 챌린지를 다시 조회 (Fetch Join으로)
		return challengeRepository.findActiveChallengeWithStatistics(userId).orElseThrow();
	}

	/**
	 * 특정 날짜의 루틴을 완료 처리하고 통계 업데이트
	 *
	 * @param challenge 대상 챌린지
	 */
	private void completeTodayRoutine(Challenge challenge) {
		List<ChallengeRoutine> todayRoutines = routineRepository
			.findByChallengeIdAndScheduledDate(challenge.getId(), ChallengeTestFixture.FIXED_START_DATE);

		assertThat(todayRoutines).isNotEmpty();

		ChallengeRoutine routine = todayRoutines.getFirst();
		routine.complete();
		routineRepository.save(routine);

		updateStatistics(challenge, 1);
	}

	/**
	 * 챌린지의 루틴을 지정된 개수만큼 완료 처리하고 통계 업데이트
	 * @param challenge 대상 챌린지
	 * @param count 완료할 루틴 개수
	 */
	private void completeRoutines(Challenge challenge, int count) {
		List<ChallengeRoutine> challengeRoutines = routineRepository.findByChallengeId(challenge.getId());
		assertThat(challengeRoutines).hasSizeGreaterThanOrEqualTo(count);

		List<ChallengeRoutine> updatedRoutines = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ChallengeRoutine routine = challengeRoutines.get(i);
			routine.complete();
			updatedRoutines.add(routine);
		}

		routineRepository.saveAll(updatedRoutines);

		updateStatistics(challenge, count);
	}

	/**
	 * 챌린지의 통계를 업데이트
	 * @param challenge 대상 챌린지
	 * @param incrementCount 증가시킬 완료 카운트
	 */
	private void updateStatistics(Challenge challenge, int incrementCount) {
		ChallengeStatistics statistics = statisticsRepository
			.findByChallengeId(challenge.getId()).orElseThrow();

		// 완료 카운트 증가
		for (int i = 0; i < incrementCount; i++) {
			statistics.incrementCompletedCount();
		}
		statistics.updateCherryLevel();
		statisticsRepository.save(statistics);

		flushAndClear();
	}

	/**
	 * 영속성 컨텍스트를 DB에 반영하고 초기화
	 */
	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}
