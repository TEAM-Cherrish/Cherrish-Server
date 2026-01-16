package com.sopt.cherrish.domain.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	void createProfileSuccess() throws Exception {
		// given
		OnboardingRequestDto request = new OnboardingRequestDto("홍길동", 25);

		OnboardingResponseDto response = new OnboardingResponseDto(1L, "홍길동", LocalDateTime.now());

		given(onboardingService.createProfile(any(OnboardingRequestDto.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/api/onboarding/profiles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(1))
			.andExpect(jsonPath("$.data.name").value("홍길동"))
			.andExpect(jsonPath("$.data.date").exists());
	}

	@Test
	@DisplayName("온보딩 프로필 생성 실패 - 유효하지 않은 입력")
	void createProfileInvalidInput() throws Exception {
		// given
		OnboardingRequestDto request = new OnboardingRequestDto("", null);

		// when & then
		mockMvc.perform(post("/api/onboarding/profiles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("온보딩 프로필 생성 실패 - 이름이 10자 초과")
	void createProfileNameTooLong() throws Exception {
		// given
		OnboardingRequestDto request = new OnboardingRequestDto("가나다라마바사아자차카", 25);

		// when & then
		mockMvc.perform(post("/api/onboarding/profiles")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}
}
