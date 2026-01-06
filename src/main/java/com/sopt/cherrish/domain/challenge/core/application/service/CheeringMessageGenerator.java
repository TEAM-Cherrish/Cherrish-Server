package com.sopt.cherrish.domain.challenge.core.application.service;

import org.springframework.stereotype.Service;

/**
 * 챌린지 응원 메시지 생성 서비스
 */
@Service
public class CheeringMessageGenerator {

	/**
	 * 현재 일차에 따른 응원 메시지 생성
	 * @param currentDay 현재 일차
	 * @param totalDays 전체 일차
	 * @return 응원 메시지
	 */
	public String generate(int currentDay, int totalDays) {
		// 챌린지 시작 전
		if (currentDay <= 0) {
			return "챌린지가 곧 시작됩니다. 준비하세요!";
		}

		// 첫째 날
		if (currentDay == 1) {
			return "챌린지 시작! 오늘부터 피부를 위한 첫 걸음입니다.";
		}

		// 중간 지점
		if (currentDay == totalDays / 2) {
			return "절반을 달성했어요! 끝까지 함께 해봐요!";
		}

		// 마지막 날
		if (currentDay >= totalDays) {
			return "마지막 날입니다! 완주까지 조금만 더 힘내세요!";
		}

		// 일반적인 경우
		return currentDay + "일차 루틴입니다. 오늘도 피부를 위해 힘내봐요!";
	}
}
