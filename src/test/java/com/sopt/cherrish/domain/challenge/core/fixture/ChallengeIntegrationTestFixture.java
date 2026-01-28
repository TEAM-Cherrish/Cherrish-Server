package com.sopt.cherrish.domain.challenge.core.fixture;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeStatistics;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRoutineRepository;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeStatisticsRepository;
import com.sopt.cherrish.domain.auth.domain.model.SocialProvider;
import com.sopt.cherrish.domain.user.domain.model.User;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestConstants.FIXED_START_DATE;
import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestConstants.DEFAULT_HOMECARE_ROUTINE;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;

/**
 * 챌린지 통합 테스트를 위한 픽스처 헬퍼 클래스
 */
@Component
public class ChallengeIntegrationTestFixture {

	private final UserRepository userRepository;
	private final ChallengeRepository challengeRepository;
	private final ChallengeRoutineRepository routineRepository;
	private final ChallengeStatisticsRepository statisticsRepository;
	private final EntityManager entityManager;

	public ChallengeIntegrationTestFixture(
		UserRepository userRepository,
		ChallengeRepository challengeRepository,
		ChallengeRoutineRepository routineRepository,
		ChallengeStatisticsRepository statisticsRepository,
		EntityManager entityManager
	) {
		this.userRepository = userRepository;
		this.challengeRepository = challengeRepository;
		this.routineRepository = routineRepository;
		this.statisticsRepository = statisticsRepository;
		this.entityManager = entityManager;
	}

	// 공통 상수는 ChallengeTestConstants에서 static import
	// FIXED_START_DATE, DEFAULT_HOMECARE_ROUTINE
	public static final String DEFAULT_USER_NAME = "테스트 유저";
	public static final int DEFAULT_USER_AGE = 25;
	public static final String OTHER_USER_NAME = "다른 유저";
	public static final int OTHER_USER_AGE = 30;
	public static final String DEFAULT_CHALLENGE_TITLE = "7일 챌린지";

	/**
	 * 기본 테스트 유저 생성
	 */
	public User createDefaultUser() {
		return createUser(DEFAULT_USER_NAME, DEFAULT_USER_AGE);
	}

	/**
	 * 다른 테스트 유저 생성
	 */
	public User createOtherUser() {
		return createUser(OTHER_USER_NAME, OTHER_USER_AGE);
	}

	/**
	 * 커스텀 테스트 유저 생성
	 */
	public User createUser(String name, int age) {
		return userRepository.save(User.builder()
			.name(name)
			.age(age)
			.socialProvider(SocialProvider.KAKAO)
			.socialId(UUID.randomUUID().toString())
			.build());
	}

	/**
	 * 챌린지와 루틴 생성 (루틴명 개수 지정)
	 *
	 * @param user 챌린지 소유자
	 * @param routineNameCount 루틴명 개수 (1-3)
	 * @return 생성된 챌린지
	 */
	public Challenge createChallengeWithRoutines(User user, int routineNameCount) {
		return createChallengeWithRoutines(user, routineNameCount, FIXED_START_DATE);
	}

	/**
	 * 챌린지와 루틴 생성 (시작일 지정)
	 *
	 * @param user 챌린지 소유자
	 * @param routineNameCount 루틴명 개수 (1-3)
	 * @param startDate 챌린지 시작일
	 * @return 생성된 챌린지 (Statistics 포함)
	 */
	public Challenge createChallengeWithRoutines(User user, int routineNameCount, LocalDate startDate) {
		// 챌린지 생성
		Challenge challenge = challengeRepository.save(Challenge.builder()
			.userId(user.getId())
			.homecareRoutine(DEFAULT_HOMECARE_ROUTINE)
			.title(DEFAULT_CHALLENGE_TITLE)
			.startDate(startDate)
			.build());

		// 루틴 생성 (예: 3개의 루틴명 × 7일 = 21개)
		List<String> routineNames = List.of("루틴1", "루틴2", "루틴3").subList(0, routineNameCount);
		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(routineNames);
		routineRepository.saveAll(routines);

		// 통계 생성
		statisticsRepository.save(ChallengeStatistics.builder()
			.challenge(challenge)
			.totalRoutineCount(routines.size())
			.build());

		// EntityManager를 통해 Challenge를 다시 로드하여 Statistics 관계를 최신화
		entityManager.flush();
		entityManager.clear();

		return challengeRepository.findActiveChallengeWithStatistics(challenge.getUserId()).orElseThrow();
	}

	/**
	 * 통계 없이 챌린지만 생성 (테스트용 특수 케이스)
	 */
	public Challenge createChallengeWithoutStatistics(User user) {
		Challenge challenge = challengeRepository.save(Challenge.builder()
			.userId(user.getId())
			.homecareRoutine(DEFAULT_HOMECARE_ROUTINE)
			.title(DEFAULT_CHALLENGE_TITLE)
			.startDate(FIXED_START_DATE)
			.build());

		List<ChallengeRoutine> routines = challenge.createChallengeRoutines(List.of("루틴1"));
		routineRepository.saveAll(routines);

		return challenge;
	}
}
