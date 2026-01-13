package com.sopt.cherrish.domain.user.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.user.presentation.dto.request.UserUpdateRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserResponseDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserSummaryResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final Clock clock;

	/**
	 * 사용자 존재 여부 검증
	 *
	 * @param id 사용자 ID
	 * @throws UserException 사용자를 찾을 수 없는 경우
	 */
	public void validateUserExists(Long id) {
		if (!userRepository.existsById(id)) {
			throw new UserException(UserErrorCode.USER_NOT_FOUND);
		}
	}

	/**
	 * 사용자 조회
	 *
	 * @param id 사용자 ID
	 * @return 사용자 요약 정보
	 * @throws UserException 사용자를 찾을 수 없는 경우
	 */
	public UserSummaryResponseDto getUser(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
		LocalDate today = LocalDate.now(clock);
		int daysSinceSignup = (int) ChronoUnit.DAYS.between(
			user.getCreatedAt().toLocalDate(),
			today
		) + 1;
		return UserSummaryResponseDto.from(user, daysSinceSignup);
	}

	/**
	 * 사용자 정보 수정 (부분 업데이트)
	 *
	 * @param id 사용자 ID
	 * @param request 수정할 정보 (name, age 중 제공된 것만 수정)
	 * @return 수정된 사용자 정보
	 * @throws UserException 사용자를 찾을 수 없는 경우
	 */
	@Transactional
	public UserResponseDto updateUser(Long id, UserUpdateRequestDto request) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		user.update(request.name(), request.age());

		return UserResponseDto.from(user);
	}

	/**
	 * 사용자 삭제 (Hard Delete)
	 *
	 * @param id 사용자 ID
	 * @throws UserException 사용자를 찾을 수 없는 경우
	 */
	@Transactional
	public void deleteUser(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
		userRepository.delete(user);
	}
}
