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
		return queryFactory
			.selectFrom(procedure)
			.distinct()
			.leftJoin(procedureWorry).on(procedureWorry.procedure.eq(procedure)).fetchJoin()
			.where(
				containsKeyword(keyword),
				eqWorryId(worryId)
			)
			.orderBy(procedure.name.asc())
			.fetch();
	}

	/**
	 * 키워드 포함 조건
	 */
	private BooleanExpression containsKeyword(String keyword) {
		return keyword != null ? procedure.name.contains(keyword) : null;
	}

	/**
	 * 피부 고민 ID 조건
	 */
	private BooleanExpression eqWorryId(Long worryId) {
		return worryId != null ? procedureWorry.worry.id.eq(worryId) : null;
	}
}
