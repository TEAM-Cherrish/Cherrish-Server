package com.sopt.cherrish.domain.challenge.core.response.success;

import com.sopt.cherrish.global.response.success.SuccessType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengeSuccessCode implements SuccessType {
	// Challenge 응답 코드 (CH001 ~ CH099)
	CHALLENGE_CREATED("CH001", "챌린지 생성 성공"),
	CHALLENGE_RETRIEVED("CH002", "챌린지 조회 성공"),
	ROUTINE_COMPLETED("CH003", "루틴을 완료했습니다!"),
	ROUTINE_UNCOMPLETED("CH004", "루틴 완료를 취소했습니다."),
	ROUTINE_BATCH_UPDATED("CH005", "루틴이 업데이트되었습니다."),
	CUSTOM_ROUTINE_ADDED("CH006", "커스텀 루틴이 추가되었습니다."),
	AI_RECOMMENDATION_GENERATED("CH007", "AI 챌린지 추천 생성 성공");

	private final String code;
	private final String message;
}
