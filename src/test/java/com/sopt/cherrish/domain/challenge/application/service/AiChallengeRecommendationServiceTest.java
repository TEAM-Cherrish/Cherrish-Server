package com.sopt.cherrish.domain.challenge.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.ai.AiClient;
import com.sopt.cherrish.domain.ai.exception.AiClientException;
import com.sopt.cherrish.domain.ai.exception.AiErrorCode;
import com.sopt.cherrish.domain.challenge.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.infrastructure.openai.response.OpenAiChallengeRecommendationResponseDto;
import com.sopt.cherrish.domain.challenge.infrastructure.prompt.ChallengePromptTemplate;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.AiRecommendationResponseDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiChallengeRecommendationService 단위 테스트")
class AiChallengeRecommendationServiceTest {

	@InjectMocks
	private AiChallengeRecommendationService aiChallengeRecommendationService;

	@Mock
	private AiClient aiClient;

	@Mock
	private ChallengePromptTemplate challengePromptTemplate;

	@Test
	@DisplayName("AI 챌린지 추천 생성 성공 - 피부 보습")
	void generateRecommendationSuccessSkinMoisturizing() {
		// given
		Long homecareRoutineId = 1L;
		String promptTemplate = "test prompt template";
		OpenAiChallengeRecommendationResponseDto mockAiResponse =
			new OpenAiChallengeRecommendationResponseDto(
				"피부 보습 7일 챌린지",
				List.of("아침 세안 후 토너 바르기", "저녁 보습 크림 충분히 바르기", "하루 8잔 물 마시기")
			);

		given(challengePromptTemplate.getChallengeRecommendationTemplate()).willReturn(promptTemplate);
		given(aiClient.call(anyString(), anyMap(), eq(OpenAiChallengeRecommendationResponseDto.class)))
			.willReturn(mockAiResponse);

		// when
		AiRecommendationResponseDto result = aiChallengeRecommendationService.generateRecommendation(
			homecareRoutineId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.challengeTitle()).isEqualTo("피부 보습 7일 챌린지");
		assertThat(result.routines()).hasSize(3);
		assertThat(result.routines()).contains("아침 세안 후 토너 바르기", "저녁 보습 크림 충분히 바르기", "하루 8잔 물 마시기");
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 성공 - 주름 개선")
	void generateRecommendationSuccessWrinkleCare() {
		// given
		Long homecareRoutineId = 3L;
		String promptTemplate = "test prompt template";
		OpenAiChallengeRecommendationResponseDto mockAiResponse =
			new OpenAiChallengeRecommendationResponseDto(
				"주름 개선 7일 챌린지",
				List.of("레티놀 세럼 바르기", "충분한 수면 취하기", "자외선 차단제 바르기", "콜라겐 음식 섭취")
			);

		given(challengePromptTemplate.getChallengeRecommendationTemplate()).willReturn(promptTemplate);
		given(aiClient.call(anyString(), anyMap(), eq(OpenAiChallengeRecommendationResponseDto.class)))
			.willReturn(mockAiResponse);

		// when
		AiRecommendationResponseDto result = aiChallengeRecommendationService.generateRecommendation(
			homecareRoutineId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.challengeTitle()).isEqualTo("주름 개선 7일 챌린지");
		assertThat(result.routines()).hasSize(4);
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 실패 - 유효하지 않은 홈케어 루틴 ID (0)")
	void generateRecommendationInvalidRoutineIdZero() {
		// given
		Long invalidRoutineId = 0L;

		// when & then
		assertThatThrownBy(() -> aiChallengeRecommendationService.generateRecommendation(invalidRoutineId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.INVALID_HOMECARE_ROUTINE_ID);
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 실패 - 유효하지 않은 홈케어 루틴 ID (7)")
	void generateRecommendationInvalidRoutineIdSeven() {
		// given
		Long invalidRoutineId = 7L;

		// when & then
		assertThatThrownBy(() -> aiChallengeRecommendationService.generateRecommendation(invalidRoutineId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.INVALID_HOMECARE_ROUTINE_ID);
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 실패 - 음수 ID")
	void generateRecommendationNegativeId() {
		// given
		Long invalidRoutineId = -1L;

		// when & then
		assertThatThrownBy(() -> aiChallengeRecommendationService.generateRecommendation(invalidRoutineId))
			.isInstanceOf(ChallengeException.class)
			.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.INVALID_HOMECARE_ROUTINE_ID);
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 실패 - AI 서비스 오류")
	void generateRecommendationAiServiceError() {
		// given
		Long homecareRoutineId = 1L;
		String promptTemplate = "test prompt template";

		given(challengePromptTemplate.getChallengeRecommendationTemplate()).willReturn(promptTemplate);
		given(aiClient.call(anyString(), anyMap(), any()))
			.willThrow(new AiClientException(AiErrorCode.AI_SERVICE_UNAVAILABLE));

		// when & then
		assertThatThrownBy(() -> aiChallengeRecommendationService.generateRecommendation(homecareRoutineId))
			.isInstanceOf(AiClientException.class)
			.hasFieldOrPropertyWithValue("errorCode", AiErrorCode.AI_SERVICE_UNAVAILABLE);
	}

	@Test
	@DisplayName("AI 챌린지 추천 생성 실패 - AI 응답 파싱 실패")
	void generateRecommendationParsingError() {
		// given
		Long homecareRoutineId = 1L;
		String promptTemplate = "test prompt template";

		given(challengePromptTemplate.getChallengeRecommendationTemplate()).willReturn(promptTemplate);
		given(aiClient.call(anyString(), anyMap(), any()))
			.willThrow(new AiClientException(AiErrorCode.AI_RESPONSE_PARSING_FAILED));

		// when & then
		assertThatThrownBy(() -> aiChallengeRecommendationService.generateRecommendation(homecareRoutineId))
			.isInstanceOf(AiClientException.class)
			.hasFieldOrPropertyWithValue("errorCode", AiErrorCode.AI_RESPONSE_PARSING_FAILED);
	}

	@Test
	@DisplayName("루틴 리스트가 빈 경우도 정상 처리")
	void generateRecommendationWithEmptyRoutines() {
		// given
		Long homecareRoutineId = 1L;
		String promptTemplate = "test prompt template";
		OpenAiChallengeRecommendationResponseDto mockAiResponse =
			new OpenAiChallengeRecommendationResponseDto(
				"피부 보습 7일 챌린지",
				List.of()
			);

		given(challengePromptTemplate.getChallengeRecommendationTemplate()).willReturn(promptTemplate);
		given(aiClient.call(anyString(), anyMap(), eq(OpenAiChallengeRecommendationResponseDto.class)))
			.willReturn(mockAiResponse);

		// when
		AiRecommendationResponseDto result = aiChallengeRecommendationService.generateRecommendation(
			homecareRoutineId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.challengeTitle()).isEqualTo("피부 보습 7일 챌린지");
		assertThat(result.routines()).isEmpty();
	}
}
