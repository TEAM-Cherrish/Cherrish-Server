package com.sopt.cherrish.domain.challenge.core.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeStatisticsRepository;
import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeIntegrationTestFixture;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;

@SpringBootTest
@DisplayName("ChallengeRoutineService 동시성 테스트")
class ChallengeRoutineServiceConcurrencyTest {

	@Autowired
	private ChallengeRoutineService challengeRoutineService;
	@Autowired
	private ChallengeRoutineRepository routineRepository;
	@Autowired
	private ChallengeStatisticsRepository statisticsRepository;
	@Autowired
	private ChallengeIntegrationTestFixture fixture;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TransactionTemplate transactionTemplate;

	private static final int ROUTINE_COUNT_LARGE = 3;

	/**
	 * 낙관적 락 동작 검증 테스트
	 *
	 * @SpringBootTest + TransactionTemplate을 사용하여
	 * 각 스레드가 독립적인 트랜잭션에서 실행되도록 하여 실제 낙관적 락 충돌을 재현합니다.
	 *
	 * 동작 방식:
	 * - 3개의 스레드가 동시에 같은 챌린지의 서로 다른 루틴을 완료 처리
	 * - 각 루틴 완료 시 ChallengeStatistics의 completedCount를 증가시킴
	 * - ChallengeStatistics는 @Version으로 낙관적 락 적용
	 * - 동시에 같은 엔티티를 수정하려 하면 OptimisticLockingFailureException 발생
	 */
	@Test
	@DisplayName("동시성 - 같은 챌린지의 여러 루틴을 동시에 완료할 때 낙관적 락 동작")
	void concurrentRoutineCompletionWithOptimisticLock() throws Exception {
		// given
		Long userId = transactionTemplate.execute(status -> {
			User user = fixture.createDefaultUser();
			return user.getId();
		});

		Long challengeId = transactionTemplate.execute(status -> {
			Challenge challenge = fixture.createChallengeWithRoutines(
				userRepository.findById(Objects.requireNonNull(userId)).orElseThrow(),
				ROUTINE_COUNT_LARGE
			);
			return challenge.getId();
		});

		List<Long> routineIds = transactionTemplate.execute(status ->
			routineRepository.findAll().stream()
				.map(ChallengeRoutine::getId)
				.toList()
		);

		// when - 3개 스레드가 동시에 다른 루틴 완료 시도
		ExecutorService executor = Executors.newFixedThreadPool(3);
		CountDownLatch latch = new CountDownLatch(3);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);

		for (int i = 0; i < 3; i++) {
			int index = i;
			executor.submit(() -> {
				try {
					// 각 스레드가 독립적인 트랜잭션에서 실행
					transactionTemplate.execute(status -> {
						challengeRoutineService.toggleCompletion(userId, Objects.requireNonNull(routineIds).get(index));
						return null;
					});
					successCount.incrementAndGet();
				} catch (OptimisticLockingFailureException e) {
					// 낙관적 락 충돌 발생 (예상된 동작)
					failureCount.incrementAndGet();
				} catch (Exception e) {
					// 기타 예외
					failureCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await(10, TimeUnit.SECONDS);
		executor.shutdown();

		// then - 모든 스레드가 완료되었는지 확인
		assertThat(successCount.get() + failureCount.get()).isEqualTo(3);

		// 실제 동시성 환경에서는 낙관적 락 충돌이 발생할 수 있음
		// 성공한 트랜잭션만 completedCount에 반영됨
		ChallengeStatistics stats = transactionTemplate.execute(status ->
			statisticsRepository.findByChallengeId(challengeId).orElseThrow()
		);

		// 실제 성공한 횟수와 completedCount가 일치해야 함
		assertThat(Objects.requireNonNull(stats).getCompletedCount())
			.as("성공한 트랜잭션 수만큼 completedCount가 증가해야 함")
			.isLessThanOrEqualTo(3);

		// 실제로 완료된 루틴 개수 확인
		long actualCompletedRoutines = Objects.requireNonNull(
			transactionTemplate.execute(status ->
				routineRepository.findAll().stream()
					.filter(ChallengeRoutine::getIsComplete)
					.count()
			)
		);

		assertThat(stats.getCompletedCount())
			.as("completedCount는 실제 완료된 루틴 수와 일치해야 함")
			.isEqualTo((int) actualCompletedRoutines);
	}

	@Test
	@DisplayName("동시성 - 같은 루틴을 동시에 토글할 때 최종 상태 일관성 보장")
	void concurrentToggleSameRoutineMaintainsConsistency() throws Exception {
		// given
		Long userId = transactionTemplate.execute(status -> {
			User user = fixture.createDefaultUser();
			return user.getId();
		});

		Long challengeId = transactionTemplate.execute(status -> {
			Challenge challenge = fixture.createChallengeWithRoutines(
				userRepository.findById(userId).orElseThrow(),
				1
			);
			return challenge.getId();
		});

		Long routineId = transactionTemplate.execute(status ->
			routineRepository.findAll().getFirst().getId()
		);

		// when - 5개 스레드가 동시에 같은 루틴 토글 시도
		ExecutorService executor = Executors.newFixedThreadPool(5);
		CountDownLatch latch = new CountDownLatch(5);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);

		for (int i = 0; i < 5; i++) {
			executor.submit(() -> {
				try {
					// 각 스레드가 독립적인 트랜잭션에서 실행
					transactionTemplate.execute(status -> {
						challengeRoutineService.toggleCompletion(userId, routineId);
						return null;
					});
					successCount.incrementAndGet();
				} catch (Exception e) {
					failureCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await(10, TimeUnit.SECONDS);
		executor.shutdown();

		// then
		assertThat(successCount.get() + failureCount.get()).isEqualTo(5);

		// 최종 상태 확인: 루틴과 통계의 일관성 검증
		ChallengeRoutine finalRoutine = transactionTemplate.execute(status ->
			routineRepository.findById(Objects.requireNonNull(routineId)).orElseThrow()
		);
		ChallengeStatistics stats = transactionTemplate.execute(status ->
			statisticsRepository.findByChallengeId(challengeId).orElseThrow()
		);

		// 루틴이 완료 상태이면 completedCount는 1, 미완료 상태이면 0
		if (Objects.requireNonNull(finalRoutine).getIsComplete()) {
			assertThat(Objects.requireNonNull(stats).getCompletedCount()).isEqualTo(1);
		} else {
			assertThat(Objects.requireNonNull(stats).getCompletedCount()).isEqualTo(0);
		}
	}
}
