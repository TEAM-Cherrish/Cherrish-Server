package com.sopt.cherrish.domain.user.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import com.sopt.cherrish.domain.user.presentation.dto.request.OnboardingRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.request.UserUpdateRequestDto;

public class UserRequestFixture {

	public static OnboardingRequestDto createOnboardingRequest(String name, Integer age) {
		try {
			var constructor = OnboardingRequestDto.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			OnboardingRequestDto request = constructor.newInstance();

			if (name != null) {
				ReflectionTestUtils.setField(request, "name", name);
			}
			if (age != null) {
				ReflectionTestUtils.setField(request, "age", age);
			}

			return request;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create OnboardingRequestDto", e);
		}
	}

	public static UserUpdateRequestDto createUpdateRequest(String name, Integer age) {
		UserUpdateRequestDto request;
		try {
			var constructor = UserUpdateRequestDto.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			request = constructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create UserUpdateRequestDto", e);
		}

		if (name != null) {
			ReflectionTestUtils.setField(request, "name", name);
		}
		if (age != null) {
			ReflectionTestUtils.setField(request, "age", age);
		}

		return request;
	}
}
