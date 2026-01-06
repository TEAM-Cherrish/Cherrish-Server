package com.sopt.cherrish.domain.ai.infrastructure.client;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.sopt.cherrish.domain.ai.AiClient;
import com.sopt.cherrish.domain.ai.exception.AiClientException;
import com.sopt.cherrish.domain.ai.exception.AiErrorCode;

import jakarta.annotation.PostConstruct;
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
		} catch (HttpClientErrorException.Unauthorized e) {
			log.error("AI service authentication failed: {}", e.getMessage(), e);
			throw new AiClientException(AiErrorCode.AI_UNAUTHORIZED, e);
		} catch (HttpClientErrorException.Forbidden e) {
			log.error("AI service access forbidden: {}", e.getMessage(), e);
			throw new AiClientException(AiErrorCode.AI_FORBIDDEN, e);
		} catch (HttpClientErrorException.NotFound e) {
			log.error("AI resource not found: {}", e.getMessage(), e);
			throw new AiClientException(AiErrorCode.AI_NOT_FOUND, e);
		} catch (HttpClientErrorException.TooManyRequests e) {
			log.error("AI rate limit exceeded: {}", e.getMessage(), e);
			throw new AiClientException(AiErrorCode.AI_RATE_LIMIT_EXCEEDED, e);
		} catch (HttpClientErrorException e) {
			log.error("AI client error (4xx): status={}, message={}", e.getStatusCode(), e.getMessage(), e);
			throw new AiClientException(AiErrorCode.AI_BAD_REQUEST, e);
		} catch (HttpServerErrorException e) {
			log.error("AI server error (5xx): status={}, message={}", e.getStatusCode(), e.getMessage(), e);
			throw new AiClientException(AiErrorCode.AI_SERVER_ERROR, e);
		} catch (ResourceAccessException e) {
			Throwable cause = e.getCause();
			if (cause instanceof SocketTimeoutException || cause instanceof TimeoutException) {
				log.error("AI request timeout: {}", e.getMessage(), e);
				throw new AiClientException(AiErrorCode.AI_TIMEOUT, e);
			} else if (cause instanceof ConnectException) {
				log.error("AI service connection refused: {}", e.getMessage(), e);
				throw new AiClientException(AiErrorCode.AI_CONNECTION_REFUSED, e);
			} else {
				log.error("AI resource access error (network/I/O): cause={}, message={}",
					cause != null ? cause.getClass().getSimpleName() : "unknown", e.getMessage(), e);
				throw new AiClientException(AiErrorCode.AI_NETWORK_ERROR, e);
			}
		} catch (NonTransientAiException e) {
			log.error("Non-transient AI error (will not retry): {}", e.getMessage(), e);
			throw new AiClientException(AiErrorCode.AI_SERVER_ERROR, e);
		} catch (Exception e) {
			log.error("Unexpected AI service error: type={}, message={}",
				e.getClass().getSimpleName(), e.getMessage(), e);
			throw new AiClientException(AiErrorCode.AI_SERVICE_UNAVAILABLE, e);
		}
	}
}
