package com.sopt.cherrish.domain.userprocedure.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

public interface UserProcedureRepositoryCustom {

	/**
	 * 특정 연월의 일자별 시술 개수 조회
	 * @param userId 사용자 ID
	 * @param year 연도
	 * @param month 월
	 * @return Map<일자, 시술 개수>
	 */
	Map<Integer, Long> findMonthlyProcedureCounts(Long userId, int year, int month);

	/**
	 * 특정 날짜의 시술 목록 조회
	 * @param userId 사용자 ID
	 * @param date 날짜
	 * @return 시술 목록
	 */
	List<UserProcedure> findDailyProcedures(Long userId, LocalDate date);
}
