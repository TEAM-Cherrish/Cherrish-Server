package com.sopt.cherrish.domain.challenge.application.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.challenge.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.infrastructure.ai.response.AiChallengeRecommendationDto;
import com.sopt.cherrish.domain.challenge.infrastructure.prompt.ChallengePromptTemplate;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.AiRecommendationResponseDto;
import com.sopt.cherrish.domain.ai.AiClient;
import com.sopt.cherrish.domain.ai.exception.AiClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiChallengeRecommendationService {

	private final AiClient aiClient;
	private final ChallengePromptTemplate challengePromptTemplate;

	/**
	 * AI 챌린지 추천 생성
	 *
	 * @param homecareRoutineId 홈케어 루틴 ID (1~6)
	 * @return AI가 생성한 챌린지 타이틀과 루틴 리스트
	 */
	public AiRecommendationResponseDto generateRecommendation(Long homecareRoutineId) {
		log.info("AI 챌린지 추천 생성 시작: homecareRoutineId={}", homecareRoutineId);

		HomecareRoutine routine;
		try {
			routine = HomecareRoutine.fromId(homecareRoutineId.intValue());
		} catch (IllegalArgumentException e) {
			log.warn("유효하지 않은 홈케어 루틴 ID: {}", homecareRoutineId);
			throw new ChallengeException(ChallengeErrorCode.INVALID_HOMECARE_ROUTINE_ID);
		}

		try {
			AiChallengeRecommendationDto aiResponse = aiClient.call(
				challengePromptTemplate.getChallengeRecommendationTemplate(),
				Map.of("homecareContent", routine.getDescription()),
				AiChallengeRecommendationDto.class
			);

			log.info("AI 챌린지 추천 생성 완료: title={}", aiResponse.challengeTitle());

			return AiRecommendationResponseDto.of(
				aiResponse.challengeTitle(),
				aiResponse.routines()
			);

		} catch (AiClientException e) {
			log.error("AI 호출 실패: homecareRoutineId={}, error={}", homecareRoutineId, e.getMessage(), e);
			throw e;
		}
	}
}
