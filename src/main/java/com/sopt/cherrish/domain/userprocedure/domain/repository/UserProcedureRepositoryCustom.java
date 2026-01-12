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

	/**
	 * 특정 기간 내의 과거 시술 조회
	 * @param userId 사용자 ID
	 * @param fromDate 시작 날짜 (이 날짜 이후 시술 조회, 당일 포함)
	 * @param toDate 기준 날짜 (이 날짜 이전 시술 조회, 당일 포함)
	 * @return 과거 시술 목록
	 */
	List<UserProcedure> findAllPastProcedures(Long userId, LocalDate fromDate, LocalDate toDate);

	/**
	 * 특정 날짜 이후의 모든 미래 시술 조회 (날짜별 그룹핑용)
	 * @param userId 사용자 ID
	 * @param fromDate 시작 날짜 (내일부터)
	 * @return 미래 시술 목록 (날짜 오름차순, 다운타임 내림차순)
	 */
	List<UserProcedure> findUpcomingProceduresGroupedByDate(Long userId, LocalDate fromDate);
}
