package com.sopt.cherrish.domain.challenge.recommendation.infrastructure.prompt;

import org.springframework.stereotype.Component;

/**
 * 챌린지 관련 AI 프롬프트 템플릿 관리 클래스
 */
@Component
public class ChallengePromptTemplate {

	private static final String CHALLENGE_RECOMMENDATION_TEMPLATE = """
		당신은 피부 홈케어 및 셀프케어 전문가입니다.
		사용자가 선택한 홈케어 루틴 카테고리: "{homecareContent}"

		위 카테고리에 맞는 매일 실천 가능한 루틴 6개를 추천해주세요.

		[작성 규칙]
		- 각 루틴은 공백 포함 20자 이내
		- 짧고 명확하게 작성

		[카테고리별 예시]
		- 피부 컨디션: "아침 닦토하기", "물 2L 마시기", "세안 후 팩토하기", "매일 밤 모델링팩 하기", "손으로 얼굴 만지지 않기", "베개 위 수건 깔기", "샤워 후 PDRN 마스크팩하기", "자기 전 페이스 괄사 마사지하기"
		- 생활습관: "일어나자마자 물 한 잔", "6시간 이상 자기", "오후 3시 이후 카페인 금지", "잠들기 30분 전 폰 내려놓기", "베개 위 수건 깔기", "12시 전에 눕기"
		- 체형관리: "기상 직후 스트레칭 10분", "물 2L 마시기", "유산소 운동하기", "저녁 식사 후 야식 안 먹기", "샤워 후 페이스 괄사"
		- 웰니스 • 마음챙김: "기상 직후 아침 명상", "일어나자마자 폰 보지 않기", "자기 전 스트레칭", "자기 전 감사일기 3가지", "내일 기대되는 것 1가지 적기"

		위 예시를 참고하되, 예시와 중복되지 않는 새로운 루틴을 추천해주세요.

		응답은 반드시 다음 JSON 형식으로만 답변하세요:
		{{
		  "routines": ["루틴1", "루틴2", "루틴3", "루틴4", "루틴5", "루틴6"]
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
