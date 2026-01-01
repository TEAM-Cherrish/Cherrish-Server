package com.sopt.cherrish.domain.user.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
import com.sopt.cherrish.domain.user.presentation.dto.request.UserUpdateRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserResponseDto;
import com.sopt.cherrish.domain.user.fixture.UserFixture;
import com.sopt.cherrish.domain.user.fixture.UserRequestFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("사용자 조회 성공")
	void getUser_Success() {
		// given
		Long userId = 1L;
		User user = UserFixture.createUser("홍길동", 25);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		UserResponseDto result = userService.getUser(userId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("홍길동");
		assertThat(result.getAge()).isEqualTo(25);
	}

	@Test
	@DisplayName("사용자 조회 실패 - 존재하지 않는 사용자")
	void getUser_NotFound() {
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
	void updateUser_Success() {
		// given
		Long userId = 1L;
		User user = UserFixture.createUser("홍길동", 25);

		UserUpdateRequestDto request = UserRequestFixture.createUpdateRequest("김철수", 30);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		UserResponseDto result = userService.updateUser(userId, request);

		// then
		assertThat(result.getName()).isEqualTo("김철수");
		assertThat(result.getAge()).isEqualTo(30);
	}

	@Test
	@DisplayName("사용자 삭제 성공")
	void deleteUser_Success() {
		// given
		Long userId = 1L;
		User user = UserFixture.createUser();

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		userService.deleteUser(userId);

		// then
		verify(userRepository, times(1)).delete(user);
	}
}
