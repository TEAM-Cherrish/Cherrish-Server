package com.sopt.cherrish.domain.challenge.recommendation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.ai.AiClient;
import com.sopt.cherrish.domain.ai.exception.AiClientException;
import com.sopt.cherrish.domain.ai.exception.AiErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.recommendation.infrastructure.openai.OpenAiChallengeRecommendationResponseDto;
import com.sopt.cherrish.domain.challenge.recommendation.infrastructure.prompt.ChallengePromptTemplate;
import com.sopt.cherrish.domain.challenge.recommendation.presentation.dto.response.AiRecommendationResponseDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiChallengeRecommendationService 단위 테스트")
class AiChallengeRecommendationServiceTest {

	private static final String TEST_PROMPT_TEMPLATE = "test prompt template";
	@InjectMocks
	private AiChallengeRecommendationService aiChallengeRecommendationService;
	@Mock
	private AiClient aiClient;
	@Mock
	private ChallengePromptTemplate challengePromptTemplate;

	private void givenAiClientReturns(OpenAiChallengeRecommendationResponseDto response) {
		given(challengePromptTemplate.getChallengeRecommendationTemplate()).willReturn(TEST_PROMPT_TEMPLATE);
		given(aiClient.call(anyString(), anyMap(), eq(OpenAiChallengeRecommendationResponseDto.class)))
			.willReturn(response);
	}

	// Fixture helper method
	private OpenAiChallengeRecommendationResponseDto createMockResponse(List<String> routines) {
		return new OpenAiChallengeRecommendationResponseDto(routines);
	}

	@Nested
	@DisplayName("AI 챌린지 추천 생성 성공")
	class GenerateRecommendationSuccess {

		@Test
		@DisplayName("피부 보습 루틴으로 챌린지 생성")
		void skinMoisturizing() {
			// given
			Integer homecareRoutineId = 1;
			OpenAiChallengeRecommendationResponseDto mockAiResponse = createMockResponse(
				List.of("아침 세안 후 토너 바르기", "저녁 보습 크림 충분히 바르기", "하루 8잔 물 마시기")
			);

			givenAiClientReturns(mockAiResponse);

			// when
			AiRecommendationResponseDto result = aiChallengeRecommendationService.generateRecommendation(
				homecareRoutineId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.routines()).hasSize(3);
			assertThat(result.routines()).containsExactly(
				"아침 세안 후 토너 바르기",
				"저녁 보습 크림 충분히 바르기",
				"하루 8잔 물 마시기"
			);

			verify(aiClient, times(1)).call(anyString(), anyMap(),
				eq(OpenAiChallengeRecommendationResponseDto.class));
		}

		@Test
		@DisplayName("주름 개선 루틴으로 챌린지 생성")
		void wrinkleCare() {
			// given
			Integer homecareRoutineId = 3;
			OpenAiChallengeRecommendationResponseDto mockAiResponse = createMockResponse(
				List.of("레티놀 세럼 바르기", "충분한 수면 취하기", "자외선 차단제 바르기", "콜라겐 음식 섭취")
			);

			givenAiClientReturns(mockAiResponse);

			// when
			AiRecommendationResponseDto result = aiChallengeRecommendationService.generateRecommendation(
				homecareRoutineId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.routines()).hasSize(4);

			verify(aiClient, times(1)).call(anyString(), anyMap(),
				eq(OpenAiChallengeRecommendationResponseDto.class));
		}

		@Test
		@DisplayName("빈 루틴 리스트도 정상 처리")
		void emptyRoutines() {
			// given
			Integer homecareRoutineId = 1;
			OpenAiChallengeRecommendationResponseDto mockAiResponse = createMockResponse(List.of());

			givenAiClientReturns(mockAiResponse);

			// when
			AiRecommendationResponseDto result = aiChallengeRecommendationService.generateRecommendation(
				homecareRoutineId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.routines()).isEmpty();

			verify(aiClient, times(1)).call(anyString(), anyMap(),
				eq(OpenAiChallengeRecommendationResponseDto.class));
		}
	}

	@Nested
	@DisplayName("AI 챌린지 추천 생성 실패")
	class GenerateRecommendationFailure {

		@ParameterizedTest
		@ValueSource(ints = {0, 7, -1, 100})
		@DisplayName("유효하지 않은 홈케어 루틴 ID")
		void invalidRoutineId(Integer invalidId) {
			// when & then
			assertThatThrownBy(() -> aiChallengeRecommendationService.generateRecommendation(invalidId))
				.isInstanceOf(ChallengeException.class)
				.hasFieldOrPropertyWithValue("errorCode", ChallengeErrorCode.INVALID_HOMECARE_ROUTINE_ID);
		}

		@Test
		@DisplayName("AI 서비스 오류")
		void aiServiceError() {
			// given
			Integer homecareRoutineId = 1;
			given(challengePromptTemplate.getChallengeRecommendationTemplate()).willReturn(TEST_PROMPT_TEMPLATE);
			given(aiClient.call(anyString(), anyMap(), any()))
				.willThrow(new AiClientException(AiErrorCode.AI_SERVICE_UNAVAILABLE));

			// when & then
			assertThatThrownBy(() -> aiChallengeRecommendationService.generateRecommendation(homecareRoutineId))
				.isInstanceOf(AiClientException.class)
				.hasFieldOrPropertyWithValue("errorCode", AiErrorCode.AI_SERVICE_UNAVAILABLE);

			verify(aiClient, times(1)).call(anyString(), anyMap(), any());
		}

		@Test
		@DisplayName("AI 응답 파싱 실패")
		void parsingError() {
			// given
			Integer homecareRoutineId = 1;
			given(challengePromptTemplate.getChallengeRecommendationTemplate()).willReturn(TEST_PROMPT_TEMPLATE);
			given(aiClient.call(anyString(), anyMap(), any()))
				.willThrow(new AiClientException(AiErrorCode.AI_RESPONSE_PARSING_FAILED));

			// when & then
			assertThatThrownBy(() -> aiChallengeRecommendationService.generateRecommendation(homecareRoutineId))
				.isInstanceOf(AiClientException.class)
				.hasFieldOrPropertyWithValue("errorCode", AiErrorCode.AI_RESPONSE_PARSING_FAILED);

			verify(aiClient, times(1)).call(anyString(), anyMap(), any());
		}
	}
}
