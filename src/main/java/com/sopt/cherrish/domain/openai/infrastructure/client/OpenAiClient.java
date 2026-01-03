package com.sopt.cherrish.domain.openai.infrastructure.client;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.openai.AiClient;

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
		PromptTemplate template = new PromptTemplate(promptTemplate);
		String prompt = template.render(variables);

		ChatClient chatClient = chatClientBuilder.build();

		return chatClient.prompt()
			.user(prompt)
			.call()
			.entity(responseType);
	}
}
