package com.sopt.cherrish.domain.challenge.core.application.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.willThrow;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeRoutineService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeService;
import com.sopt.cherrish.domain.challenge.core.application.service.ChallengeStatisticsService;
import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.model.ChallengeRoutine;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.request.ChallengeCreateRequestDto;
import com.sopt.cherrish.domain.challenge.core.presentation.dto.response.ChallengeCreateResponseDto;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeCreationFacade 단위 테스트")
class ChallengeCreationFacadeTest {

	@InjectMocks
	private ChallengeCreationFacade challengeCreationFacade;

	@Mock
	private ChallengeService challengeService;

	@Mock
	private ChallengeRoutineService routineService;

	@Mock
	private ChallengeStatisticsService statisticsService;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("성공 - 챌린지 생성 전체 플로우 정상 동작")
	void createChallengeSuccess() {
		// given
		Long userId = 1L;
		ChallengeCreateRequestDto request = ChallengeTestFixture.createValidChallengeRequest();

		User mockUser = User.builder().name("테스트").age(25).build();
		Challenge mockChallenge = ChallengeTestFixture.createChallenge(1L, userId);
		List<ChallengeRoutine> mockRoutines = ChallengeTestFixture.createChallengeRoutines(
			mockChallenge, request.routineNames());

		given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
		given(challengeService.createChallenge(eq(userId), any(HomecareRoutine.class), eq(request.title()), any()))
			.willReturn(mockChallenge);
		given(routineService.createAndSaveRoutines(mockChallenge, request.routineNames()))
			.willReturn(mockRoutines);

		// when
		ChallengeCreateResponseDto response = challengeCreationFacade.createChallenge(userId, request);

		// then
		assertThat(response.challengeId()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("7일 챌린지");
		assertThat(response.totalDays()).isEqualTo(7);
		assertThat(response.totalRoutineCount()).isEqualTo(21);

		then(userRepository).should(times(1)).findById(userId);
		then(challengeService).should(times(1)).validateNoDuplicateActiveChallenge(userId);
		then(challengeService).should(times(1)).createChallenge(eq(userId), any(), eq(request.title()), any());
		then(routineService).should(times(1)).createAndSaveRoutines(mockChallenge, request.routineNames());
		then(statisticsService).should(times(1)).initializeStatistics(eq(mockChallenge), anyList());
	}

	@Test
	@DisplayName("실패 - 존재하지 않는 사용자")
	void failUserNotFound() {
		// given
		Long userId = 999L;
		ChallengeCreateRequestDto request = ChallengeTestFixture.createValidChallengeRequest();

		given(userRepository.findById(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> challengeCreationFacade.createChallenge(userId, request))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

		then(challengeService).should(times(0)).validateNoDuplicateActiveChallenge(any());
	}

	@Test
	@DisplayName("실패 - 이미 활성 챌린지 존재")
	void failDuplicateActiveChallenge() {
		// given
		Long userId = 1L;
		ChallengeCreateRequestDto request = ChallengeTestFixture.createValidChallengeRequest();

		User mockUser = User.builder().name("테스트").age(25).build();

		given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
		willThrow(new ChallengeException(ChallengeErrorCode.DUPLICATE_ACTIVE_CHALLENGE))
			.given(challengeService).validateNoDuplicateActiveChallenge(userId);

		// when & then
		assertThatThrownBy(() -> challengeCreationFacade.createChallenge(userId, request))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.DUPLICATE_ACTIVE_CHALLENGE);

		then(challengeService).should(times(0)).createChallenge(any(), any(), any(), any());
	}
}
