package com.sopt.cherrish.domain.openai;

import java.util.Map;

/**
 * AI 서비스 클라이언트 인터페이스
 * 다양한 AI 제공자(OpenAI, Claude, Gemini 등)를 추상화
 */
public interface AiClient {

	/**
	 * AI API를 호출하여 구조화된 응답 받기
	 *
	 * @param promptTemplate 프롬프트 템플릿 문자열
	 * @param variables 템플릿에 삽입할 변수 맵
	 * @param responseType 응답 타입 클래스
	 * @param <T> 응답 타입
	 * @return AI가 생성한 구조화된 응답
	 */
	<T> T call(String promptTemplate, Map<String, Object> variables, Class<T> responseType);
}
