package com.sopt.cherrish.domain.challenge.infrastructure.prompt;

import org.springframework.stereotype.Component;

/**
 * 챌린지 관련 AI 프롬프트 템플릿 관리 클래스
 */
@Component
public class ChallengePromptTemplate {

	private static final String CHALLENGE_RECOMMENDATION_TEMPLATE = """
		당신은 피부 홈케어 전문가입니다.
		사용자가 선택한 홈케어 루틴 카테고리: "{homecareContent}"

		위 카테고리에 맞는 7일 챌린지를 설계해주세요.

		응답 형식:
		1. 챌린지 제목: 흥미롭고 동기부여가 되는 한글 제목 (최대 30자)
		2. 실천 루틴 리스트: 하루 3-5개의 구체적인 실천 항목 (각 최대 50자)

		응답은 반드시 다음 JSON 형식으로만 답변하세요:
		{{
		  "challenge_title": "챌린지 제목",
		  "routines": ["루틴1", "루틴2", "루틴3", "루틴4", "루틴5"]
		}}
		""";

	/**
	 * 챌린지 추천 프롬프트 템플릿 반환
	 *
	 * @return 챌린지 추천용 프롬프트 템플릿 문자열
	 */
	public String getChallengeRecommendationTemplate() {
		return CHALLENGE_RECOMMENDATION_TEMPLATE;
	}
}
