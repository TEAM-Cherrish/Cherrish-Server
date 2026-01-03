package com.sopt.cherrish.domain.challenge.application.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.application.dto.AiChallengeRecommendation;
import com.sopt.cherrish.domain.challenge.infrastructure.prompt.ChallengePromptTemplate;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.AiRecommendationResponseDto;
import com.sopt.cherrish.domain.openai.infrastructure.client.OpenAiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiChallengeRecommendationService {

	private final OpenAiClient openAiClient;
	private final ChallengePromptTemplate challengePromptTemplate;

	/**
	 * AI 챌린지 추천 생성 (1단계: 하드코딩된 카테고리)
	 *
	 * @param homecareRoutineId 홈케어 루틴 ID (현재는 사용 안 함, 2단계에서 DB 조회용)
	 * @return AI가 생성한 챌린지 타이틀과 루틴 리스트
	 */
	public AiRecommendationResponseDto generateRecommendation(Long homecareRoutineId) {
		log.info("AI 챌린지 추천 생성 시작: homecareRoutineId={}", homecareRoutineId);

		// TODO: 2단계에서 homecareRoutineId로 DB 조회하여 실제 카테고리 가져오기
		// 1단계: 하드코딩된 카테고리로 테스트
		String testCategory = "피부 보습 관리";

		try {
			AiChallengeRecommendation aiResponse = openAiClient.call(
				challengePromptTemplate.getChallengeRecommendationTemplate(),
				Map.of("homecareContent", testCategory),
				AiChallengeRecommendation.class
			);

			log.info("AI 챌린지 추천 생성 완료: title={}", aiResponse.challengeTitle());

			return AiRecommendationResponseDto.of(
				aiResponse.challengeTitle(),
				aiResponse.routines()
			);

		} catch (Exception e) {
			log.error("AI 호출 실패: homecareRoutineId={}, error={}", homecareRoutineId, e.getMessage(), e);
			throw new RuntimeException("AI 서비스를 일시적으로 사용할 수 없습니다.", e);
		}
	}
}
