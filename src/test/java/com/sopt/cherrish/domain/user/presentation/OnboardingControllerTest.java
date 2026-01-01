package com.sopt.cherrish.domain.user.presentation;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sopt.cherrish.domain.user.application.service.OnboardingService;
import com.sopt.cherrish.domain.user.presentation.dto.request.OnboardingRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.OnboardingResponseDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.OnboardingTodayCareDto;

@WebMvcTest(OnboardingController.class)
@DisplayName("OnboardingController 통합 테스트")
class OnboardingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OnboardingService onboardingService;

	@Test
	@DisplayName("온보딩 프로필 생성 성공")
	void createProfile_Success() throws Exception {
		// given
		OnboardingRequestDto request = createOnboardingRequest("홍길동", 25);

		OnboardingResponseDto response = OnboardingResponseDto.builder()
			.name("홍길동")
			.date(LocalDateTime.now())
			.weeklyStreak(null)
			.todayStatus(null)
			.todayCare(OnboardingTodayCareDto.empty())
			.build();

		given(onboardingService.createProfile(any(OnboardingRequestDto.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/api/onboarding/profiles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("홍길동"))
			.andExpect(jsonPath("$.data.weeklyStreak").isEmpty())
			.andExpect(jsonPath("$.data.todayStatus").isEmpty())
			.andExpect(jsonPath("$.data.todayCare.routines").isArray());
	}

	@Test
	@DisplayName("온보딩 프로필 생성 실패 - 유효하지 않은 입력")
	void createProfile_InvalidInput() throws Exception {
		// given
		OnboardingRequestDto request = createOnboardingRequest("", null);

		// when & then
		mockMvc.perform(post("/api/onboarding/profiles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	private OnboardingRequestDto createOnboardingRequest(String name, Integer age) {
		try {
			var constructor = OnboardingRequestDto.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			OnboardingRequestDto request = constructor.newInstance();

			if (name != null) {
				var nameField = OnboardingRequestDto.class.getDeclaredField("name");
				nameField.setAccessible(true);
				nameField.set(request, name);
			}

			if (age != null) {
				var ageField = OnboardingRequestDto.class.getDeclaredField("age");
				ageField.setAccessible(true);
				ageField.set(request, age);
			}

			return request;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create OnboardingRequestDto", e);
		}
	}
}
