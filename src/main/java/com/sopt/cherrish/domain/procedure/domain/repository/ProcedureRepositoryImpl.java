package com.sopt.cherrish.domain.procedure.domain.repository;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sopt.cherrish.domain.procedure.domain.model.Procedure;

import lombok.RequiredArgsConstructor;

import static com.sopt.cherrish.domain.procedure.domain.model.QProcedure.procedure;
import static com.sopt.cherrish.domain.procedure.domain.model.QProcedureWorry.procedureWorry;

@RequiredArgsConstructor
public class ProcedureRepositoryImpl implements ProcedureRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Procedure> searchProcedures(String keyword, Long worryId) {
		// worryId가 있으면 inner join, 없으면 일반 조회
		if (worryId != null) {
			return queryFactory
				.selectDistinct(procedure)
				.from(procedureWorry)
				.join(procedureWorry.procedure, procedure)
				.where(
					procedureWorry.worry.id.eq(worryId),
					containsKeyword(keyword)
				)
				.fetch();
		}

		return queryFactory
			.selectFrom(procedure)
			.where(containsKeyword(keyword))
			.fetch();
	}

	@Override
	public List<Procedure> findByIdInAndWorryId(List<Long> ids, Long worryId) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}

		if (worryId != null) {
			return queryFactory
				.selectDistinct(procedure)
				.from(procedureWorry)
				.join(procedureWorry.procedure, procedure)
				.where(
					procedure.id.in(ids),
					procedureWorry.worry.id.eq(worryId)
				)
				.fetch();
		}

		return queryFactory
			.selectFrom(procedure)
			.where(procedure.id.in(ids))
			.fetch();
	}

	/**
	 * 키워드 포함 조건
	 */
	private BooleanExpression containsKeyword(String keyword) {
		return keyword != null && !keyword.isBlank() ? procedure.name.contains(keyword) : null;
	}
}
