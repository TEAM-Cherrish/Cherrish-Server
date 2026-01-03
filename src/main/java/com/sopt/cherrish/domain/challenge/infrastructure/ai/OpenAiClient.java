package com.sopt.cherrish.domain.challenge.infrastructure.ai;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import com.sopt.cherrish.domain.challenge.application.dto.AiChallengeRecommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

	private final ChatClient.Builder chatClientBuilder;
	private final ChallengePromptTemplate challengePromptTemplate;

	/**
	 * OpenAI API를 호출하여 챌린지 추천 받기
	 *
	 * @param homecareContent 홈케어 루틴 카테고리 내용
	 * @return AI가 생성한 챌린지 추천
	 */
	public AiChallengeRecommendation generateChallengeRecommendation(String homecareContent) {
		log.debug("OpenAI API 호출 시작: homecareContent={}", homecareContent);

		PromptTemplate promptTemplate = new PromptTemplate(
			challengePromptTemplate.getChallengeRecommendationTemplate()
		);
		String prompt = promptTemplate.render(Map.of("homecareContent", homecareContent));

		ChatClient chatClient = chatClientBuilder.build();

		AiChallengeRecommendation response = chatClient.prompt()
			.user(prompt)
			.call()
			.entity(AiChallengeRecommendation.class);

		log.debug("OpenAI API 호출 완료: challengeTitle={}", response.challengeTitle());

		return response;
	}
}
