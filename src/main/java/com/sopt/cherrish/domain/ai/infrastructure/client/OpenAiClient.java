package com.sopt.cherrish.domain.ai.infrastructure.client;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.ai.AiClient;
import com.sopt.cherrish.domain.ai.exception.AiClientException;
import com.sopt.cherrish.domain.ai.exception.AiErrorCode;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * OpenAI API 클라이언트 구현체
 * AiClient 인터페이스를 OpenAI로 구현
 */
@Component
@RequiredArgsConstructor
public class OpenAiClient implements AiClient {

	private final ChatClient.Builder chatClientBuilder;
	private ChatClient chatClient;

	/**
	 * ChatClient 초기화
	 * 의존성 주입 후 한 번만 빌드하여 모든 요청에서 재사용
	 */
	@PostConstruct
	private void initializeChatClient() {
		this.chatClient = chatClientBuilder.build();
	}

	@Override
	public <T> T call(String promptTemplate, Map<String, Object> variables, Class<T> responseType) {
		// 프롬프트 템플릿 파싱 및 렌더링 (template-related errors)
		String prompt;
		try {
			PromptTemplate template = new PromptTemplate(promptTemplate);
			prompt = template.render(variables);
		} catch (IllegalArgumentException e) {
			throw new AiClientException(AiErrorCode.INVALID_PROMPT_TEMPLATE);
		}

		// AI 호출 및 응답 파싱
		try {
			T response;
			try {
				response = chatClient.prompt()
					.user(prompt)
					.call()
					.entity(responseType);
			} catch (IllegalArgumentException e) {
				// entity(responseType) 호출 시 발생하는 IllegalArgumentException
				// (응답 파싱, 타입 변환, 메타데이터 검증 실패 등)
				throw new AiClientException(AiErrorCode.AI_RESPONSE_PARSING_FAILED);
			}

			if (response == null) {
				throw new AiClientException(AiErrorCode.AI_RESPONSE_PARSING_FAILED);
			}

			return response;

		} catch (AiClientException e) {
			throw e;
		} catch (Exception e) {
			throw new AiClientException(AiErrorCode.AI_SERVICE_UNAVAILABLE);
		}
	}
}
