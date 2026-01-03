package com.sopt.cherrish.domain.openai.infrastructure.client;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.openai.AiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI API 클라이언트 구현체
 * AiClient 인터페이스를 OpenAI로 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient implements AiClient {

	private final ChatClient.Builder chatClientBuilder;

	@Override
	public <T> T call(String promptTemplate, Map<String, Object> variables, Class<T> responseType) {
		log.debug("OpenAI API 호출 시작: responseType={}", responseType.getSimpleName());

		PromptTemplate template = new PromptTemplate(promptTemplate);
		String prompt = template.render(variables);

		ChatClient chatClient = chatClientBuilder.build();

		T response = chatClient.prompt()
			.user(prompt)
			.call()
			.entity(responseType);

		log.debug("OpenAI API 호출 완료: responseType={}", responseType.getSimpleName());

		return response;
	}
}
