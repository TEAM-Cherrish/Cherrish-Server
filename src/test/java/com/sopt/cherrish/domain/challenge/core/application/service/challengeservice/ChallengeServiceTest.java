package com.sopt.cherrish.domain.challenge.core.application.service.challengeservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeService 단위 테스트")
class ChallengeServiceTest {

	@Mock
	private ChallengeRepository challengeRepository;

	@InjectMocks
	private ChallengeService challengeService;

	@Test
	@DisplayName("성공 - 활성 챌린지가 없을 때 중복 검증 통과")
	void validateNoDuplicateActiveChallengeSuccess() {
		// given
		Long userId = 1L;
		when(challengeRepository.existsByUserIdAndIsActiveTrue(userId))
			.thenReturn(false);

		// when & then
		challengeService.validateNoDuplicateActiveChallenge(userId);

		verify(challengeRepository).existsByUserIdAndIsActiveTrue(userId);
	}

	@Test
	@DisplayName("실패 - 이미 활성 챌린지가 있을 때 중복 검증 실패")
	void validateNoDuplicateActiveChallengeDuplicateExistsThrowsException() {
		// given
		Long userId = 1L;
		when(challengeRepository.existsByUserIdAndIsActiveTrue(userId))
			.thenReturn(true);

		// when & then
		assertThatThrownBy(() -> challengeService.validateNoDuplicateActiveChallenge(userId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.DUPLICATE_ACTIVE_CHALLENGE);
	}

	@Test
	@DisplayName("성공 - 챌린지 생성 및 저장")
	void createChallengeSuccess() {
		// given
		Long userId = 1L;
		HomecareRoutine routine = HomecareRoutine.SKIN_MOISTURIZING;
		LocalDate startDate = LocalDate.now();

		Challenge expectedChallenge = Challenge.builder()
			.userId(userId)
			.homecareRoutine(routine)
			.title(routine.getDescription())
			.startDate(startDate)
			.build();

		when(challengeRepository.save(any(Challenge.class)))
			.thenReturn(expectedChallenge);

		// when
		Challenge result = challengeService.createChallenge(userId, routine, startDate);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getUserId()).isEqualTo(userId);
		assertThat(result.getHomecareRoutine()).isEqualTo(routine);
		assertThat(result.getTitle()).isEqualTo(routine.getDescription());
		assertThat(result.getStartDate()).isEqualTo(startDate);
		verify(challengeRepository).save(any(Challenge.class));
	}

	@Test
	@DisplayName("성공 - 활성 챌린지 조회")
	void getActiveChallengeSuccess() {
		// given
		Long userId = 1L;
		Challenge activeChallenge = ChallengeTestFixture.createDefaultChallenge(userId);

		when(challengeRepository.findByUserIdAndIsActiveTrue(userId))
			.thenReturn(Optional.of(activeChallenge));

		// when
		Challenge result = challengeService.getActiveChallenge(userId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getUserId()).isEqualTo(userId);
		assertThat(result.getIsActive()).isTrue();
		verify(challengeRepository).findByUserIdAndIsActiveTrue(userId);
	}

	@Test
	@DisplayName("실패 - 활성 챌린지가 없을 때 조회 실패")
	void getActiveChallengeNotFoundThrowsException() {
		// given
		Long userId = 999L;
		when(challengeRepository.findByUserIdAndIsActiveTrue(userId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> challengeService.getActiveChallenge(userId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND);
	}

	@Test
	@DisplayName("성공 - ID로 챌린지 조회")
	void getChallengeByIdSuccess() {
		// given
		Long challengeId = 1L;
		Long userId = 1L;
		Challenge challenge = ChallengeTestFixture.createDefaultChallenge(userId);

		when(challengeRepository.findById(challengeId))
			.thenReturn(Optional.of(challenge));

		// when
		Challenge result = challengeService.getChallengeById(challengeId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getUserId()).isEqualTo(userId);
		assertThat(result.getTitle()).isEqualTo("피부 보습 관리");
		assertThat(result.getIsActive()).isTrue();
		verify(challengeRepository).findById(challengeId);
	}

	@Test
	@DisplayName("실패 - ID로 챌린지 조회 시 챌린지를 찾을 수 없음")
	void getChallengeByIdNotFoundThrowsException() {
		// given
		Long challengeId = 999L;
		when(challengeRepository.findById(challengeId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> challengeService.getChallengeById(challengeId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.CHALLENGE_NOT_FOUND);
	}
}
