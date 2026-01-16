package com.sopt.cherrish.domain.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.sopt.cherrish.domain.user.application.service.UserService;
import com.sopt.cherrish.domain.user.presentation.dto.request.UserUpdateRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserResponseDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserSummaryResponseDto;

@WebMvcTest(UserController.class)
@DisplayName("UserController 통합 테스트")
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@Test
	@DisplayName("사용자 조회 성공")
	void getUserSuccess() throws Exception {
		// given
		Long userId = 1L;
		UserSummaryResponseDto response = new UserSummaryResponseDto("홍길동", 3);

		given(userService.getUser(userId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/users")
				.header("X-User-Id", userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("홍길동"))
			.andExpect(jsonPath("$.data.daysSinceSignup").value(3));
	}

	@Test
	@DisplayName("사용자 정보 수정 성공")
	void updateUserSuccess() throws Exception {
		// given
		Long userId = 1L;
		UserUpdateRequestDto request = new UserUpdateRequestDto("김철수", 30);

		UserResponseDto response = new UserResponseDto(userId, "김철수", 30, LocalDateTime.now(), LocalDateTime.now());

		given(userService.updateUser(eq(userId), any(UserUpdateRequestDto.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/users")
				.header("X-User-Id", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("김철수"))
			.andExpect(jsonPath("$.data.age").value(30));
	}

	@Test
	@DisplayName("사용자 삭제 성공")
	void deleteUserSuccess() throws Exception {
		// given
		Long userId = 1L;
		willDoNothing().given(userService).deleteUser(userId);

		// when & then
		mockMvc.perform(delete("/api/users")
				.header("X-User-Id", userId))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("사용자 정보 수정 실패 - 이름이 7자 초과")
	void updateUserNameTooLong() throws Exception {
		// given
		Long userId = 1L;
		UserUpdateRequestDto request = new UserUpdateRequestDto("가나다라마바사아", 30);

		// when & then
		mockMvc.perform(patch("/api/users")
				.header("X-User-Id", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}
}
