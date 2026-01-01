package com.sopt.cherrish.domain.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.presentation.dto.request.OnboardingRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.OnboardingResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

	private final UserRepository userRepository;

	/**
	 * 온보딩: 새 사용자 프로필 생성
	 *
	 * @param request 온보딩 요청 (name, age)
	 * @return 온보딩 응답 (weeklyStreak: null, todayStatus: null, todayCare.routines: [])
	 */
	@Transactional
	public OnboardingResponseDto createProfile(OnboardingRequestDto request) {
		// 1. Request DTO -> Entity 변환
		User user = request.toEntity();

		// 2. Entity 저장
		User savedUser = userRepository.save(user);

		// 3. Entity -> Response DTO 변환 (온보딩 형식)
		return OnboardingResponseDto.from(savedUser);
	}
}
