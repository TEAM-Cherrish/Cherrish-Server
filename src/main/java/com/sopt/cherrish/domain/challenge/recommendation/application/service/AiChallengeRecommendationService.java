package com.sopt.cherrish.domain.challenge.recommendation.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sopt.cherrish.domain.ai.AiClient;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.challenge.recommendation.infrastructure.openai.OpenAiChallengeRecommendationResponseDto;
import com.sopt.cherrish.domain.challenge.recommendation.infrastructure.prompt.ChallengePromptTemplate;
import com.sopt.cherrish.domain.challenge.recommendation.presentation.dto.response.AiRecommendationResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChallengeRecommendationService {

	private static final int ROUTINE_COUNT = 6;

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

		List<String> routines = adjustRoutineCount(aiResponse.routines());

		log.info("AI 챌린지 추천 생성 완료: routines={}", routines);

		return AiRecommendationResponseDto.of(routines);
	}

	private List<String> adjustRoutineCount(List<String> routines) {
		if (routines.size() > ROUTINE_COUNT) {
			log.warn("AI 응답 루틴 개수 초과: {}개 -> {}개로 조정", routines.size(), ROUTINE_COUNT);
			return routines.subList(0, ROUTINE_COUNT);
		}
		if (routines.size() < ROUTINE_COUNT) {
			log.warn("AI 응답 루틴 개수 부족: {}개", routines.size());
		}
		return routines;
	}
}
