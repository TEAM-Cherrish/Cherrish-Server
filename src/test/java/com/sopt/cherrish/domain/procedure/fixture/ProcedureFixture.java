package com.sopt.cherrish.domain.procedure.fixture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

public class ProcedureFixture {

	private static final String DEFAULT_NAME = "레이저 토닝";
	private static final String DEFAULT_CATEGORY = "레이저";
	private static final int DEFAULT_MIN_DOWNTIME = 1;
	private static final int DEFAULT_MAX_DOWNTIME = 5;
	private static final Long DEFAULT_ID = 1L;

	private ProcedureFixture() {
		// Utility class
	}

	public static Procedure createProcedure() {
		return createProcedure(DEFAULT_NAME, DEFAULT_CATEGORY, DEFAULT_MIN_DOWNTIME, DEFAULT_MAX_DOWNTIME);
	}

	public static Procedure createProcedure(String name, String category, int minDowntimeDays, int maxDowntimeDays) {
		Procedure procedure = Procedure.builder()
			.name(name)
			.category(category)
			.minDowntimeDays(minDowntimeDays)
			.maxDowntimeDays(maxDowntimeDays)
			.build();
		setField(procedure, Procedure.class, "id", DEFAULT_ID);
		setField(procedure, BaseTimeEntity.class, "createdAt", LocalDateTime.now());
		setField(procedure, BaseTimeEntity.class, "updatedAt", LocalDateTime.now());
		return procedure;
	}

	private static void setField(Object target, Class<?> clazz, String fieldName, Object value) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("Failed to set " + fieldName + " field", e);
		}
	}
}
