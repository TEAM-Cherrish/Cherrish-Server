package com.sopt.cherrish.domain.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.presentation.dto.request.UserUpdateRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserResponseDto;
import com.sopt.cherrish.global.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본값: 읽기 전용 트랜잭션
public class UserService {

	private final UserRepository userRepository;

	/**
	 * 사용자 조회
	 *
	 * @param id 사용자 ID
	 * @return 사용자 정보
	 * @throws BaseException 사용자를 찾을 수 없는 경우
	 */
	public UserResponseDto getUser(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
		return UserResponseDto.from(user);
	}

	/**
	 * 사용자 정보 수정 (부분 업데이트)
	 *
	 * @param id 사용자 ID
	 * @param request 수정할 정보 (name, age 중 제공된 것만 수정)
	 * @return 수정된 사용자 정보
	 * @throws BaseException 사용자를 찾을 수 없는 경우
	 */
	@Transactional
	public UserResponseDto updateUser(Long id, UserUpdateRequestDto request) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

		user.update(request.getName(), request.getAge());

		return UserResponseDto.from(user);
	}

	/**
	 * 사용자 삭제 (Hard Delete)
	 *
	 * @param id 사용자 ID
	 * @throws BaseException 사용자를 찾을 수 없는 경우
	 */
	@Transactional
	public void deleteUser(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
		userRepository.delete(user);
	}
}
