package com.sopt.cherrish.domain.ai.infrastructure.client;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.ai.AiClient;
import com.sopt.cherrish.domain.ai.exception.AiClientException;
import com.sopt.cherrish.domain.ai.exception.AiErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * OpenAI API 클라이언트 구현체
 * AiClient 인터페이스를 OpenAI로 구현
 */
@Component
@RequiredArgsConstructor
public class OpenAiClient implements AiClient {

	private final ChatClient.Builder chatClientBuilder;

	@Override
	public <T> T call(String promptTemplate, Map<String, Object> variables, Class<T> responseType) {
		try {
			PromptTemplate template = new PromptTemplate(promptTemplate);
			String prompt = template.render(variables);

			ChatClient chatClient = chatClientBuilder.build();

			T response = chatClient.prompt()
				.user(prompt)
				.call()
				.entity(responseType);

			if (response == null) {
				throw new AiClientException(AiErrorCode.AI_RESPONSE_PARSING_FAILED);
			}

			return response;

		} catch (IllegalArgumentException e) {
			throw new AiClientException(AiErrorCode.INVALID_PROMPT_TEMPLATE);
		} catch (AiClientException e) {
			throw e;
		} catch (Exception e) {
			throw new AiClientException(AiErrorCode.AI_SERVICE_UNAVAILABLE);
		}
	}
}
