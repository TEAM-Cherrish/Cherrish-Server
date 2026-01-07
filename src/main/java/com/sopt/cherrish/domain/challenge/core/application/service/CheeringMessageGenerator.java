package com.sopt.cherrish.domain.challenge.core.application.service;

import org.springframework.stereotype.Service;

/**
 * 챌린지 응원 메시지 생성 서비스
 */
@Service
public class CheeringMessageGenerator {

	// 메시지 상수
	public static final String PREPARATION_MESSAGE = "챌린지가 곧 시작됩니다. 준비하세요!";
	public static final String FIRST_DAY_MESSAGE = "챌린지 시작! 오늘부터 피부를 위한 첫 걸음입니다.";
	public static final String HALFWAY_MESSAGE = "절반을 달성했어요! 끝까지 함께 해봐요!";
	public static final String LAST_DAY_MESSAGE = "마지막 날입니다! 완주까지 조금만 더 힘내세요!";

	/**
	 * 현재 일차에 따른 응원 메시지 생성
	 * @param currentDay 현재 일차
	 * @param totalDays 전체 일차
	 * @return 응원 메시지
	 * @throws IllegalArgumentException totalDays가 0 이하인 경우
	 */
	public String generate(int currentDay, int totalDays) {
		// 입력 유효성 검증
		if (totalDays <= 0) {
			throw new IllegalArgumentException("totalDays는 0보다 커야 합니다: " + totalDays);
		}

		// 챌린지 시작 전
		if (currentDay <= 0) {
			return PREPARATION_MESSAGE;
		}

		// 첫째 날
		if (currentDay == 1) {
			return FIRST_DAY_MESSAGE;
		}

		// 중간 지점 (totalDays가 2 이상일 때만 체크)
		if (totalDays > 1 && currentDay == totalDays / 2) {
			return HALFWAY_MESSAGE;
		}

		// 마지막 날
		if (currentDay >= totalDays) {
			return LAST_DAY_MESSAGE;
		}

		// 일반적인 경우
		return currentDay + "일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!";
	}
}
