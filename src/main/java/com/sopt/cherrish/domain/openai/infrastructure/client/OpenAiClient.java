package com.sopt.cherrish.domain.openai.infrastructure.client;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI API 클라이언트
 * 범용 AI 서비스 호출을 담당하는 인프라 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

	private final ChatClient.Builder chatClientBuilder;

	/**
	 * OpenAI API를 호출하여 구조화된 응답 받기
	 *
	 * @param promptTemplate 프롬프트 템플릿 문자열
	 * @param variables 템플릿에 삽입할 변수 맵
	 * @param responseType 응답 타입 클래스
	 * @param <T> 응답 타입
	 * @return AI가 생성한 구조화된 응답
	 */
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
