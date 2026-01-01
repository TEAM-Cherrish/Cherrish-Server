package com.sopt.cherrish.domain.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.presentation.dto.request.UserUpdateRequest;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserResponse;
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
	public UserResponse getUser(Long id) {
		User user = findUserById(id);
		return UserResponse.from(user);
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
	public UserResponse updateUser(Long id, UserUpdateRequest request) {
		User user = findUserById(id);

		// Entity의 update 메서드를 통해 수정 (Dirty Checking)
		user.update(request.getName(), request.getAge());

		return UserResponse.from(user);
	}

	/**
	 * 사용자 삭제 (Hard Delete)
	 *
	 * @param id 사용자 ID
	 * @throws BaseException 사용자를 찾을 수 없는 경우
	 */
	@Transactional
	public void deleteUser(Long id) {
		User user = findUserById(id);
		userRepository.delete(user);
	}

	// 공통 조회 메서드
	private User findUserById(Long id) {
		return userRepository.findById(id)
			.orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
	}
}
