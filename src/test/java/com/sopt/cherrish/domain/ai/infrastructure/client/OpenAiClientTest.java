package com.sopt.cherrish.domain.ai.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

import com.sopt.cherrish.domain.ai.exception.AiClientException;
import com.sopt.cherrish.domain.ai.exception.AiErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiClient 단위 테스트")
class OpenAiClientTest {

	@InjectMocks
	private OpenAiClient openAiClient;

	@Mock
	private ChatClient.Builder chatClientBuilder;

	@Mock
	private ChatClient chatClient;

	@Mock
	private ChatClientRequestSpec requestSpec;

	@Mock
	private CallResponseSpec responseSpec;

	@SuppressWarnings("unchecked")
	private void givenChatClientReturns(Object response) {
		given(chatClientBuilder.build()).willReturn(chatClient);
		given(chatClient.prompt()).willReturn(requestSpec);
		given(requestSpec.user(anyString())).willReturn(requestSpec);
		given(requestSpec.call()).willReturn(responseSpec);
		given(responseSpec.entity(any(Class.class))).willReturn(response);
	}

	private record TestResponseDto(String message) {
	}

	@Nested
	@DisplayName("AI 호출 성공")
	class CallSuccess {

		@Test
		@DisplayName("정상 응답 반환")
		void successfulResponse() {
			// given
			String promptTemplate = "테스트 프롬프트: {topic}";
			Map<String, Object> variables = Map.of("topic", "피부 관리");
			TestResponseDto expectedResponse = new TestResponseDto("테스트 응답");

			givenChatClientReturns(expectedResponse);

			// when
			TestResponseDto result = openAiClient.call(promptTemplate, variables, TestResponseDto.class);

			// then
			assertThat(result).isNotNull();
			assertThat(result.message()).isEqualTo("테스트 응답");
		}
	}

	@Nested
	@DisplayName("AI 호출 실패")
	class CallFailure {

		@Test
		@DisplayName("null 응답 시 AI_RESPONSE_PARSING_FAILED 예외 발생")
		void nullResponse() {
			// given
			String promptTemplate = "테스트 프롬프트";
			Map<String, Object> variables = Map.of();

			givenChatClientReturns(null);

			// when & then
			assertThatThrownBy(() -> openAiClient.call(promptTemplate, variables, TestResponseDto.class))
				.isInstanceOf(AiClientException.class)
				.hasFieldOrPropertyWithValue("errorCode", AiErrorCode.AI_RESPONSE_PARSING_FAILED);
		}

		@Test
		@DisplayName("AI 서비스 오류 시 AI_SERVICE_UNAVAILABLE 예외 발생")
		void aiServiceError() {
			// given
			String promptTemplate = "테스트 프롬프트";
			Map<String, Object> variables = Map.of();

			given(chatClientBuilder.build()).willReturn(chatClient);
			given(chatClient.prompt()).willReturn(requestSpec);
			given(requestSpec.user(anyString())).willReturn(requestSpec);
			given(requestSpec.call()).willThrow(new RuntimeException("AI 서비스 오류"));

			// when & then
			assertThatThrownBy(() -> openAiClient.call(promptTemplate, variables, TestResponseDto.class))
				.isInstanceOf(AiClientException.class)
				.hasFieldOrPropertyWithValue("errorCode", AiErrorCode.AI_SERVICE_UNAVAILABLE);
		}
	}
}
