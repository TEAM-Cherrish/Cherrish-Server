package com.sopt.cherrish.domain.challenge.core.application.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

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
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.user.domain.model.User;

import static com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture.DEFAULT_HOMECARE_ROUTINE;
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
	ChallengeCreationFacade.class,
	ChallengeService.class,
	ChallengeRoutineService.class,
	ChallengeStatisticsService.class
})
@DisplayName("ChallengeCreationFacade 통합 테스트")
class ChallengeCreationFacadeIntegrationTest {

	@Autowired
	private ChallengeCreationFacade challengeCreationFacade;

	@Autowired
	private ChallengeRepository challengeRepository;

	@Autowired
	private ChallengeRoutineRepository routineRepository;

	@Autowired
	private ChallengeStatisticsRepository statisticsRepository;

	@Autowired
	private UserRepository userRepository;

	private User createTestUser() {
		return userRepository.save(User.builder()
			.name("테스트 유저")
			.age(25)
			.build());
	}

	@Test
	@DisplayName("성공 - 챌린지 생성 전체 플로우 (DB 저장 확인)")
	void createChallengeSuccessSavesToDatabase() {
		// given
		User user = createTestUser();

		ChallengeCreateRequestDto request = new ChallengeCreateRequestDto(
			DEFAULT_HOMECARE_ROUTINE.getId(),
			List.of("아침 세안", "토너 바르기", "크림 바르기")
		);

		// when
		ChallengeCreateResponseDto response = challengeCreationFacade.createChallenge(user.getId(), request);

		// then - Response 검증
		assertThat(response.challengeId()).isNotNull();
		assertThat(response.title()).isEqualTo(DEFAULT_HOMECARE_ROUTINE.getDescription());
		assertThat(response.totalDays()).isEqualTo(7);
		assertThat(response.totalRoutineCount()).isEqualTo(21); // 3 × 7
		assertThat(response.routines()).hasSize(21);

		// then - DB 실제 저장 확인
		Challenge savedChallenge = challengeRepository.findById(response.challengeId()).orElseThrow();
		assertThat(savedChallenge.getUserId()).isEqualTo(user.getId());
		assertThat(savedChallenge.getHomecareRoutine()).isEqualTo(DEFAULT_HOMECARE_ROUTINE);
		assertThat(savedChallenge.getIsActive()).isTrue();

		List<ChallengeRoutine> savedRoutines = routineRepository.findAll();
		assertThat(savedRoutines).hasSize(21);
		assertThat(savedRoutines).allMatch(routine -> !routine.getIsComplete());

		ChallengeStatistics savedStatistics = statisticsRepository.findByChallengeId(savedChallenge.getId())
			.orElseThrow();
		assertThat(savedStatistics.getCompletedCount()).isEqualTo(0);
		assertThat(savedStatistics.getTotalRoutineCount()).isEqualTo(21);
	}

	@Test
	@DisplayName("성공 - 단일 루틴명으로 챌린지 생성 (1개 × 7일 = 7개)")
	void createChallengeSingleRoutineCreates7routines() {
		// given
		User user = createTestUser();

		ChallengeCreateRequestDto request = new ChallengeCreateRequestDto(
			DEFAULT_HOMECARE_ROUTINE.getId(),
			List.of("아침 보습")
		);

		// when
		ChallengeCreateResponseDto response = challengeCreationFacade.createChallenge(user.getId(), request);

		// then
		assertThat(response.totalRoutineCount()).isEqualTo(7);
		assertThat(routineRepository.findAll()).hasSize(7);
	}

	@Test
	@DisplayName("실패 - 존재하지 않는 사용자")
	void createChallengeUserNotFoundThrowsException() {
		// given
		Long nonExistentUserId = 999L;
		ChallengeCreateRequestDto request = new ChallengeCreateRequestDto(
			1,
			List.of("세안")
		);

		// when & then
		assertThatThrownBy(() -> challengeCreationFacade.createChallenge(nonExistentUserId, request))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

		// DB에 아무것도 저장되지 않았는지 확인
		assertThat(challengeRepository.findAll()).isEmpty();
		assertThat(routineRepository.findAll()).isEmpty();
		assertThat(statisticsRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("실패 - 이미 활성 챌린지가 존재함")
	void createChallengeDuplicateActiveChallengeThrowsException() {
		// given
		User user = createTestUser();

		// 이미 활성 챌린지가 존재
		challengeRepository.save(Challenge.builder()
			.userId(user.getId())
			.homecareRoutine(DEFAULT_HOMECARE_ROUTINE)
			.title("기존 챌린지")
			.startDate(java.time.LocalDate.now())
			.build());

		ChallengeCreateRequestDto request = new ChallengeCreateRequestDto(
			1,
			List.of("세안")
		);

		// when & then
		assertThatThrownBy(() -> challengeCreationFacade.createChallenge(user.getId(), request))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.DUPLICATE_ACTIVE_CHALLENGE);

		// 기존 챌린지만 있고 새 챌린지는 생성되지 않음
		assertThat(challengeRepository.findAll()).hasSize(1);
	}

	@Test
	@DisplayName("실패 - 잘못된 홈케어 루틴 ID")
	void createChallengeInvalidHomecareRoutineIdThrowsException() {
		// given
		User user = createTestUser();

		ChallengeCreateRequestDto request = new ChallengeCreateRequestDto(
			999, // 존재하지 않는 ID
			List.of("세안")
		);

		// when & then
		assertThatThrownBy(() -> challengeCreationFacade.createChallenge(user.getId(), request))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.INVALID_HOMECARE_ROUTINE_ID);
	}

	@Test
	@DisplayName("트랜잭션 롤백 - 예외 발생 시 모든 변경사항 롤백")
	void createChallengeExceptionOccursRollbacksAllChanges() {
		// given
		User user = createTestUser();

		// 이미 활성 챌린지 존재
		challengeRepository.save(Challenge.builder()
			.userId(user.getId())
			.homecareRoutine(DEFAULT_HOMECARE_ROUTINE)
			.title("기존 챌린지")
			.startDate(java.time.LocalDate.now())
			.build());

		ChallengeCreateRequestDto request = new ChallengeCreateRequestDto(
			1,
			List.of("세안")
		);

		long initialChallengeCount = challengeRepository.count();
		long initialRoutineCount = routineRepository.count();
		long initialStatisticsCount = statisticsRepository.count();

		// when
		try {
			challengeCreationFacade.createChallenge(user.getId(), request);
		} catch (ChallengeException e) {
			// expected
		}

		// then - 개수가 변하지 않음 (롤백됨)
		assertThat(challengeRepository.count()).isEqualTo(initialChallengeCount);
		assertThat(routineRepository.count()).isEqualTo(initialRoutineCount);
		assertThat(statisticsRepository.count()).isEqualTo(initialStatisticsCount);
	}
}
