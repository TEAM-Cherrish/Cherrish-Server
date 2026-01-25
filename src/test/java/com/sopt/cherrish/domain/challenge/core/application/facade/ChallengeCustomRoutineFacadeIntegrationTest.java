package com.sopt.cherrish.domain.challenge.core.application.facade;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_CHALLENGE_TITLE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_HOMECARE_ROUTINE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.FIXED_START_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeStatisticsService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeStatisticsRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.CustomRoutineAddRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.CustomRoutineAddResponseDto;
import com.sopt.cherrish.domain.auth.domain.model.SocialProvider;
import com.sopt.cherrish.domain.user.application.service.UserService;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.global.config.QueryDslConfig;
import com.sopt.cherrish.global.config.TestClockConfig;
import com.sopt.cherrish.global.config.TestJpaAuditConfig;

@DataJpaTest
@Import({
	TestJpaAuditConfig.class,
	TestClockConfig.class,
	QueryDslConfig.class,
	ChallengeCustomRoutineFacade.class,
	ChallengeService.class,
	ChallengeRoutineService.class,
	ChallengeStatisticsService.class,
	UserService.class
})
@DisplayName("ChallengeCustomRoutineFacade 통합 테스트")
class ChallengeCustomRoutineFacadeIntegrationTest {

	@Autowired
	private ChallengeCustomRoutineFacade challengeCustomRoutineFacade;

	@Autowired
	private ChallengeRepository challengeRepository;

	@Autowired
	private ChallengeRoutineRepository routineRepository;

	@Autowired
	private ChallengeStatisticsRepository statisticsRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private jakarta.persistence.EntityManager entityManager;

	private User createTestUser() {
		return userRepository.save(User.builder()
			.name("테스트 유저")
			.age(25)
			.socialProvider(SocialProvider.KAKAO)
			.socialId(UUID.randomUUID().toString())
			.build());
	}

	private Challenge createActiveChallengeWithRoutines(User user, int initialRoutineCount) {
		// FIXED_START_DATE(2024-01-01)부터 2024-01-07까지 챌린지
		Challenge challenge = challengeRepository.save(Challenge.builder()
			.userId(user.getId())
			.homecareRoutine(DEFAULT_HOMECARE_ROUTINE)
			.title(DEFAULT_CHALLENGE_TITLE)
			.startDate(FIXED_START_DATE)
			.build());

		// 초기 루틴 생성 (1일차부터 7일차까지)
		for (int day = 0; day < 7; day++) {
			for (int i = 0; i < initialRoutineCount; i++) {
				routineRepository.save(ChallengeRoutine.builder()
					.challenge(challenge)
					.name("기본 루틴 " + (i + 1))
					.scheduledDate(challenge.getStartDate().plusDays(day))
					.build());
			}
		}

		// 통계 생성
		statisticsRepository.save(ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(initialRoutineCount * 7) // 3개 루틴 × 7일
			.build());

		// 영속성 컨텍스트 비우기 - 이후 조회 시 DB에서 Fetch Join 수행
		entityManager.flush();
		entityManager.clear();

		return challenge;
	}

	@Test
	@DisplayName("성공 - 커스텀 루틴 추가 전체 플로우 (DB 저장 확인)")
	void addCustomRoutineSuccessSavesToDatabase() {
		// given
		User user = createTestUser();
		Challenge challenge = createActiveChallengeWithRoutines(user, 3);

		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");

		// when - 오늘이 2024-01-01 (1일차, TestClockConfig에서 고정) -> 1일차부터 7일차까지 7개 생성
		CustomRoutineAddResponseDto response = challengeCustomRoutineFacade.addCustomRoutine(
			user.getId(),
			request
		);

		// then - Response 검증
		assertThat(response.challengeId()).isEqualTo(challenge.getId());
		assertThat(response.routineName()).isEqualTo("저녁 마사지");
		assertThat(response.addedCount()).isEqualTo(7); // 2024-01-01 ~ 2024-01-07 = 7일
		assertThat(response.routines()).hasSize(7);
		assertThat(response.totalRoutineCount()).isEqualTo(28); // 기존 21 + 추가 7 = 28

		// then - DB 실제 저장 확인
		List<ChallengeRoutine> allRoutines = routineRepository.findAll();
		assertThat(allRoutines).hasSize(28); // 기존 21 + 새로 추가된 7

		List<ChallengeRoutine> customRoutines = allRoutines.stream()
			.filter(r -> r.getName().equals("저녁 마사지"))
			.toList();
		assertThat(customRoutines).hasSize(7);
		assertThat(customRoutines).allMatch(r -> !r.getIsComplete());

		ChallengeStatistics savedStatistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		assertThat(savedStatistics.getTotalRoutineCount()).isEqualTo(28);
	}

	@Test
	@DisplayName("실패 - 존재하지 않는 사용자")
	void addCustomRoutineUserNotFoundThrowsException() {
		// given
		Long nonExistentUserId = 999L;
		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");

		// when & then
		assertThatThrownBy(() -> challengeCustomRoutineFacade.addCustomRoutine(nonExistentUserId, request))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

		// DB에 아무것도 추가되지 않음
		assertThat(routineRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("실패 - 활성 챌린지가 존재하지 않음")
	void addCustomRoutineNoChallengeThrowsException() {
		// given
		User user = createTestUser();
		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");

		// when & then
		assertThatThrownBy(() -> challengeCustomRoutineFacade.addCustomRoutine(user.getId(), request))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND);
	}

	@Test
	@DisplayName("실패 - 비활성 챌린지에 추가 시도")
	void addCustomRoutineInactiveChallengeThrowsException() {
		// given
		User user = createTestUser();
		Challenge challenge = createActiveChallengeWithRoutines(user, 3);

		// 챌린지를 비활성화
		challenge.complete();
		challengeRepository.save(challenge);

		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");

		long initialRoutineCount = routineRepository.count();

		// when & then
		assertThatThrownBy(() -> challengeCustomRoutineFacade.addCustomRoutine(user.getId(), request))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND);

		// 루틴이 추가되지 않음
		assertThat(routineRepository.count()).isEqualTo(initialRoutineCount);
	}

	@Test
	@DisplayName("실패 - 다른 사용자의 챌린지에 접근")
	void addCustomRoutineUnauthorizedAccessThrowsException() {
		// given
		User owner = createTestUser();
		User other = userRepository.save(User.builder()
			.name("다른 유저")
			.age(30)
			.socialProvider(SocialProvider.KAKAO)
			.socialId(UUID.randomUUID().toString())
			.build());

		createActiveChallengeWithRoutines(owner, 3);

		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");

		// when & then
		assertThatThrownBy(() -> challengeCustomRoutineFacade.addCustomRoutine(other.getId(), request))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND); // 다른 유저의 활성 챌린지를 찾을 수 없음
	}

	@Test
	@DisplayName("성공 - 통계 업데이트 확인 (totalRoutineCount 증가, cherryLevel 재계산)")
	void addCustomRoutineUpdatesStatistics() {
		// given
		User user = createTestUser();
		Challenge challenge = createActiveChallengeWithRoutines(user, 3);

		ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		int initialTotalRoutineCount = statistics.getTotalRoutineCount();
		assertThat(initialTotalRoutineCount).isEqualTo(21);

		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 운동");

		// when
		CustomRoutineAddResponseDto response = challengeCustomRoutineFacade.addCustomRoutine(
			user.getId(),
			request
		);

		// then
		assertThat(response.totalRoutineCount()).isEqualTo(28); // 21 + 7

		ChallengeStatistics updatedStatistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		assertThat(updatedStatistics.getTotalRoutineCount()).isEqualTo(28);
		// cherryLevel은 completedCount / totalRoutineCount로 계산되므로 totalRoutineCount 증가 시 재계산됨
	}

	@Test
	@DisplayName("트랜잭션 롤백 - 예외 발생 시 모든 변경사항 롤백")
	void addCustomRoutineExceptionOccursRollbacksAllChanges() {
		// given
		User user = createTestUser();
		Challenge challenge = createActiveChallengeWithRoutines(user, 3);

		// 챌린지를 비활성화
		challenge.complete();
		challengeRepository.save(challenge);

		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("저녁 마사지");

		long initialRoutineCount = routineRepository.count();
		ChallengeStatistics initialStatistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		int initialTotalRoutineCount = initialStatistics.getTotalRoutineCount();

		// when
		try {
			challengeCustomRoutineFacade.addCustomRoutine(user.getId(), request);
		} catch (ChallengeException e) {
			// expected
		}

		// then - 개수가 변하지 않음 (롤백됨)
		assertThat(routineRepository.count()).isEqualTo(initialRoutineCount);

		ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		assertThat(statistics.getTotalRoutineCount()).isEqualTo(initialTotalRoutineCount);
	}

	@Test
	@DisplayName("성공 - 오늘 날짜에 19개 루틴 → 커스텀 1개 추가 가능 (제한-1)")
	void addCustomRoutineSuccessWhen19RoutinesExist() {
		// given
		User user = createTestUser();
		Challenge challenge = createActiveChallengeWithRoutines(user, 3);

		// 오늘(2024-01-01)에 추가로 루틴 생성 (기존 3개 + 추가 = MAX-1개)
		int additionalRoutines = ChallengeCustomRoutineFacade.MAX_DAILY_ROUTINE_COUNT - 3 - 1;
		for (int i = 0; i < additionalRoutines; i++) {
			routineRepository.save(ChallengeRoutine.builder()
				.challenge(challenge)
				.name("추가 루틴 " + (i + 1))
				.scheduledDate(FIXED_START_DATE) // 오늘
				.build());
		}

		// 통계 업데이트
		ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		statistics.incrementTotalRoutineCount(additionalRoutines);
		statisticsRepository.save(statistics);

		entityManager.flush();
		entityManager.clear();

		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("마지막 루틴");

		// when - 오늘 MAX-1개 → MAX번째 추가 (성공)
		CustomRoutineAddResponseDto response = challengeCustomRoutineFacade.addCustomRoutine(
			user.getId(),
			request
		);

		// then
		assertThat(response.routineName()).isEqualTo("마지막 루틴");
		assertThat(response.addedCount()).isEqualTo(7);

		// 오늘 날짜의 루틴 개수 확인
		long todayRoutineCount = routineRepository.countByChallengeIdAndScheduledDate(
			challenge.getId(),
			FIXED_START_DATE
		);
		assertThat(todayRoutineCount).isEqualTo(ChallengeCustomRoutineFacade.MAX_DAILY_ROUTINE_COUNT);
	}

	@Test
	@DisplayName("실패 - 오늘 날짜에 MAX개 루틴 → 커스텀 추가 불가 (제한 초과)")
	void addCustomRoutineFailsWhen20RoutinesExist() {
		// given
		User user = createTestUser();
		Challenge challenge = createActiveChallengeWithRoutines(user, 3);

		// 오늘(2024-01-01)에 추가로 루틴 생성 (기존 3개 + 추가 = MAX개)
		int additionalRoutines = ChallengeCustomRoutineFacade.MAX_DAILY_ROUTINE_COUNT - 3;
		for (int i = 0; i < additionalRoutines; i++) {
			routineRepository.save(ChallengeRoutine.builder()
				.challenge(challenge)
				.name("추가 루틴 " + (i + 1))
				.scheduledDate(FIXED_START_DATE) // 오늘
				.build());
		}

		// 통계 업데이트
		ChallengeStatistics statistics = statisticsRepository.findByChallengeId(challenge.getId())
			.orElseThrow();
		statistics.incrementTotalRoutineCount(additionalRoutines);
		statisticsRepository.save(statistics);

		entityManager.flush();
		entityManager.clear();

		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("초과 루틴");

		// when & then - 오늘 MAX개 → MAX+1번째 추가 시도 (실패)
		assertThatThrownBy(() -> challengeCustomRoutineFacade.addCustomRoutine(user.getId(), request))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CUSTOM_ROUTINE_LIMIT_EXCEEDED);

		// 루틴이 추가되지 않음
		long todayRoutineCount = routineRepository.countByChallengeIdAndScheduledDate(
			challenge.getId(),
			FIXED_START_DATE
		);
		assertThat(todayRoutineCount).isEqualTo(ChallengeCustomRoutineFacade.MAX_DAILY_ROUTINE_COUNT);
	}

	@Test
	@DisplayName("성공 - 오늘 날짜에 3개 루틴 (기본) → 커스텀 추가 가능")
	void addCustomRoutineSuccessWithDefaultRoutines() {
		// given
		User user = createTestUser();
		Challenge challenge = createActiveChallengeWithRoutines(user, 3);

		CustomRoutineAddRequestDto request = new CustomRoutineAddRequestDto("커스텀 루틴");

		// when - 오늘 3개 → 커스텀 추가 (성공)
		CustomRoutineAddResponseDto response = challengeCustomRoutineFacade.addCustomRoutine(
			user.getId(),
			request
		);

		// then
		assertThat(response.routineName()).isEqualTo("커스텀 루틴");
		assertThat(response.addedCount()).isEqualTo(7);

		// 오늘 날짜의 루틴 개수 확인
		long todayRoutineCount = routineRepository.countByChallengeIdAndScheduledDate(
			challenge.getId(),
			FIXED_START_DATE
		);
		assertThat(todayRoutineCount).isEqualTo(4); // 3 + 1 = 4
	}
}
