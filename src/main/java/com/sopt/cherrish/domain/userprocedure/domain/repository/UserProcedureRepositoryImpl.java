package com.sopt.cherrish.domain.userprocedure.domain.repository;

import static com.sopt.cherrish.domain.userprocedure.domain.model.QUserProcedure.userProcedure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
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

		List<Tuple> results = queryFactory
			.select(
				userProcedure.scheduledAt.dayOfMonth(),
				userProcedure.count()
			)
			.from(userProcedure)
			.where(
				userProcedure.user.id.eq(userId),
				userProcedure.scheduledAt.goe(startOfMonth),
				userProcedure.scheduledAt.lt(endOfMonth)
			)
			.groupBy(userProcedure.scheduledAt.dayOfMonth())
			.fetch();

		return results.stream()
			.collect(Collectors.toMap(
				tuple -> tuple.get(userProcedure.scheduledAt.dayOfMonth()),
				tuple -> tuple.get(userProcedure.count())
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
