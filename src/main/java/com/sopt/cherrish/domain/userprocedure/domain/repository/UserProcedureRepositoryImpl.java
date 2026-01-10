package com.sopt.cherrish.domain.userprocedure.domain.repository;

import static com.sopt.cherrish.domain.userprocedure.domain.model.QUserProcedure.userProcedure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserProcedureRepositoryImpl implements UserProcedureRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Map<Integer, Long> findMonthlyProcedureCounts(Long userId, int year, int month) {
		LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
		LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

		List<UserProcedure> procedures = queryFactory
			.selectFrom(userProcedure)
			.where(
				userProcedure.user.id.eq(userId),
				userProcedure.scheduledAt.goe(startOfMonth),
				userProcedure.scheduledAt.lt(endOfMonth)
			)
			.fetch();

		// 일자별로 그룹핑하여 개수 집계
		return procedures.stream()
			.collect(Collectors.groupingBy(
				procedure -> procedure.getScheduledAt().getDayOfMonth(),
				Collectors.counting()
			));
	}

	@Override
	public List<UserProcedure> findDailyProcedures(Long userId, LocalDate date) {
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

		return queryFactory
			.selectFrom(userProcedure)
			.join(userProcedure.procedure).fetchJoin()
			.where(
				userProcedure.user.id.eq(userId),
				userProcedure.scheduledAt.goe(startOfDay),
				userProcedure.scheduledAt.lt(endOfDay)
			)
			.orderBy(userProcedure.scheduledAt.asc())
			.fetch();
	}
}
