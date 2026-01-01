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
import com.sopt.cherrish.domain.user.application.service.UserService;
import com.sopt.cherrish.domain.user.presentation.dto.request.UserUpdateRequestDto;
import com.sopt.cherrish.domain.user.presentation.dto.response.UserResponseDto;

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
	void getUser_Success() throws Exception {
		// given
		Long userId = 1L;
		UserResponseDto response = UserResponseDto.builder()
			.id(userId)
			.name("홍길동")
			.age(25)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		given(userService.getUser(userId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/users/{id}", userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("홍길동"))
			.andExpect(jsonPath("$.data.age").value(25));
	}

	@Test
	@DisplayName("사용자 정보 수정 성공")
	void updateUser_Success() throws Exception {
		// given
		Long userId = 1L;
		UserUpdateRequestDto request = createUpdateRequest("김철수", 30);

		UserResponseDto response = UserResponseDto.builder()
			.id(userId)
			.name("김철수")
			.age(30)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		given(userService.updateUser(eq(userId), any(UserUpdateRequestDto.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/users/{id}", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("김철수"))
			.andExpect(jsonPath("$.data.age").value(30));
	}

	@Test
	@DisplayName("사용자 삭제 성공")
	void deleteUser_Success() throws Exception {
		// given
		Long userId = 1L;
		willDoNothing().given(userService).deleteUser(userId);

		// when & then
		mockMvc.perform(delete("/api/users/{id}", userId))
			.andExpect(status().isOk());
	}

	private UserUpdateRequestDto createUpdateRequest(String name, Integer age) {
		try {
			var constructor = UserUpdateRequestDto.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			UserUpdateRequestDto request = constructor.newInstance();

			if (name != null) {
				var nameField = UserUpdateRequestDto.class.getDeclaredField("name");
				nameField.setAccessible(true);
				nameField.set(request, name);
			}

			if (age != null) {
				var ageField = UserUpdateRequestDto.class.getDeclaredField("age");
				ageField.setAccessible(true);
				ageField.set(request, age);
			}

			return request;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create UserUpdateRequestDto", e);
		}
	}
}
