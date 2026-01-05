package com.sopt.cherrish.domain.challenge.core.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRepository;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.core.fixture.ChallengeTestFixture;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeService 단위 테스트")
class ChallengeServiceTest {

	@InjectMocks
	private ChallengeService challengeService;

	@Mock
	private ChallengeRepository challengeRepository;

	@Test
	@DisplayName("챌린지 생성 성공")
	void createChallengeSuccess() {
		// given
		Long userId = 1L;
		Challenge mockChallenge = ChallengeTestFixture.createChallenge(1L, userId);

		given(challengeRepository.save(any(Challenge.class))).willReturn(mockChallenge);

		// when
		Challenge result = challengeService.createChallenge(
			userId, HomecareRoutine.SKIN_MOISTURIZING, "7일 챌린지", LocalDate.now());

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getUserId()).isEqualTo(userId);
	}

	@Test
	@DisplayName("중복 활성 챌린지 검증 - 성공 (중복 없음)")
	void validateNoDuplicateSuccess() {
		// given
		Long userId = 1L;
		given(challengeRepository.existsByUserIdAndIsActiveTrue(userId)).willReturn(false);

		// when & then - 예외 발생하지 않음
		challengeService.validateNoDuplicateActiveChallenge(userId);
	}

	@Test
	@DisplayName("중복 활성 챌린지 검증 - 실패 (이미 활성 챌린지 존재)")
	void validateNoDuplicateFail() {
		// given
		Long userId = 1L;
		given(challengeRepository.existsByUserIdAndIsActiveTrue(userId)).willReturn(true);

		// when & then
		assertThatThrownBy(() -> challengeService.validateNoDuplicateActiveChallenge(userId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.DUPLICATE_ACTIVE_CHALLENGE);
	}
}
