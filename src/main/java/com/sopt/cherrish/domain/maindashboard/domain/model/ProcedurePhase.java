package com.sopt.cherrish.domain.maindashboard.domain.model;

import java.time.LocalDate;

import com.sopt.cherrish.domain.userprocedure.domain.vo.DowntimePeriod;

/**
 * 시술 후 회복 단계
 */
public enum ProcedurePhase {
	SENSITIVE,   // 민감기
	CAUTION,     // 주의기
	RECOVERY,    // 회복기
	COMPLETED;   // 완료 (다운타임 종료)

	/**
	 * 다운타임 기간과 현재 날짜를 기반으로 현재 단계 계산
	 *
	 * @param downtimePeriod 다운타임 기간
	 * @param today 현재 날짜
	 * @return 현재 시술 단계
	 */
	public static ProcedurePhase calculate(DowntimePeriod downtimePeriod, LocalDate today) {
		if (downtimePeriod.getSensitiveDays().contains(today)) {
			return SENSITIVE;
		}
		if (downtimePeriod.getCautionDays().contains(today)) {
			return CAUTION;
		}
		if (downtimePeriod.getRecoveryDays().contains(today)) {
			return RECOVERY;
		}
		return COMPLETED;
	}
}
