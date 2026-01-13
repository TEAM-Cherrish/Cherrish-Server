package com.sopt.cherrish.domain.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.user.fixture.UserFixture;
import com.sopt.cherrish.domain.user.presentation.dto.request.UserUpdateRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserResponseDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserSummaryResponseDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private Clock clock;

	@Test
	@DisplayName("사용자 조회 성공")
	void getUserSuccess() {
		// given
		Long userId = 1L;
		User user = UserFixture.createUser("홍길동", 25);
		ZoneId zoneId = ZoneId.of("Asia/Seoul");
		Instant fixedInstant = user.getCreatedAt().atZone(zoneId).toInstant();

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(clock.getZone()).willReturn(zoneId);
		given(clock.instant()).willReturn(fixedInstant);

		// when
		UserSummaryResponseDto result = userService.getUser(userId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("홍길동");
		assertThat(result.getDaysSinceSignup()).isEqualTo(1);
	}

	@Test
	@DisplayName("사용자 조회 실패 - 존재하지 않는 사용자")
	void getUserNotFound() {
		// given
		Long userId = 999L;
		given(userRepository.findById(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.getUser(userId))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("사용자 정보 수정 성공")
	void updateUserSuccess() {
		// given
		Long userId = 1L;
		User user = UserFixture.createUser("홍길동", 25);

		UserUpdateRequestDto request = new UserUpdateRequestDto("김철수", 30);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		UserResponseDto result = userService.updateUser(userId, request);

		// then
		assertThat(result.getName()).isEqualTo("김철수");
		assertThat(result.getAge()).isEqualTo(30);
	}

	@Test
	@DisplayName("사용자 정보 수정 실패 - 존재하지 않는 사용자")
	void updateUserNotFound() {
		// given
		Long userId = 999L;
		UserUpdateRequestDto request = new UserUpdateRequestDto("김철수", 30);
		given(userRepository.findById(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.updateUser(userId, request))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("사용자 삭제 성공")
	void deleteUserSuccess() {
		// given
		Long userId = 1L;
		User user = UserFixture.createUser();

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		userService.deleteUser(userId);

		// then
		verify(userRepository, times(1)).delete(user);
	}

	@Test
	@DisplayName("사용자 삭제 실패 - 존재하지 않는 사용자")
	void deleteUserNotFound() {
		// given
		Long userId = 999L;
		given(userRepository.findById(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.deleteUser(userId))
			.isInstanceOf(UserException.class)
			.hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
	}
}
