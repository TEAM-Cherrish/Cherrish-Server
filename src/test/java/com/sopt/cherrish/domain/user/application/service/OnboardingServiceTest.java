package com.sopt.cherrish.domain.user.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.presentation.dto.request.OnboardingRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.OnboardingResponseDto;
import com.sopt.cherrish.domain.user.fixture.UserFixture;
import com.sopt.cherrish.domain.user.fixture.UserRequestFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingService 단위 테스트")
class OnboardingServiceTest {

	@InjectMocks
	private OnboardingService onboardingService;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("온보딩 프로필 생성 성공")
	void createProfile_Success() {
		// given
		OnboardingRequestDto request = UserRequestFixture.createOnboardingRequest("홍길동", 25);
		User savedUser = UserFixture.createUser("홍길동", 25);

		given(userRepository.save(any(User.class))).willReturn(savedUser);

		// when
		OnboardingResponseDto result = onboardingService.createProfile(request);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("홍길동");
		assertThat(result.getWeeklyStreak()).isNull();
		assertThat(result.getTodayStatus()).isNull();
		assertThat(result.getTodayCare().getRoutines()).isEmpty();
	}
}
