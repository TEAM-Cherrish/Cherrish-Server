package com.sopt.cherrish.domain.challenge.application.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.sopt.cherrish.domain.ai.AiClient;
import com.sopt.cherrish.domain.challenge.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.challenge.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.infrastructure.openai.response.OpenAiChallengeRecommendationResponseDto;
import com.sopt.cherrish.domain.challenge.infrastructure.prompt.ChallengePromptTemplate;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.AiRecommendationResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChallengeRecommendationService {

	private final AiClient aiClient;
	private final ChallengePromptTemplate challengePromptTemplate;

	/**
	 * AI 챌린지 추천 생성
	 *
	 * @param homecareRoutineId 홈케어 루틴 ID (1~6)
	 * @return AI가 생성한 챌린지 타이틀과 루틴 리스트
	 */
	public AiRecommendationResponseDto generateRecommendation(Integer homecareRoutineId) {
		log.info("AI 챌린지 추천 생성 시작: homecareRoutineId={}", homecareRoutineId);

		if (homecareRoutineId == null) {
			log.warn("홈케어 루틴 ID가 null입니다");
			throw new ChallengeException(ChallengeErrorCode.INVALID_HOMECARE_ROUTINE_ID);
		}

		HomecareRoutine routine;
		try {
			routine = HomecareRoutine.fromId(homecareRoutineId);
		} catch (IllegalArgumentException e) {
			log.warn("유효하지 않은 홈케어 루틴 ID: {}", homecareRoutineId);
			throw new ChallengeException(ChallengeErrorCode.INVALID_HOMECARE_ROUTINE_ID);
		}

		OpenAiChallengeRecommendationResponseDto aiResponse = aiClient.call(
			challengePromptTemplate.getChallengeRecommendationTemplate(),
			Map.of("homecareContent", routine.getDescription()),
			OpenAiChallengeRecommendationResponseDto.class
		);

		log.info("AI 챌린지 추천 생성 완료: title={}", aiResponse.challengeTitle());

		return AiRecommendationResponseDto.of(
			aiResponse.challengeTitle(),
			aiResponse.routines()
		);
	}
}
