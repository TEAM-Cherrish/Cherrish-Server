package com.sopt.cherrish.domain.userprocedure.fixture;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

public class UserProcedureFixture {

	private static final AtomicLong ID_GENERATOR = new AtomicLong(1L);

	private UserProcedureFixture() {
		// Utility class
	}

	public static UserProcedure createUserProcedure(
		User user,
		Procedure procedure,
		LocalDateTime scheduledAt,
		Integer downtimeDays
	) {
		return createUserProcedure(ID_GENERATOR.getAndIncrement(), user, procedure, scheduledAt, downtimeDays, null);
	}

	public static UserProcedure createUserProcedure(
		Long id,
		User user,
		Procedure procedure,
		LocalDateTime scheduledAt,
		Integer downtimeDays
	) {
		return createUserProcedure(id, user, procedure, scheduledAt, downtimeDays, null);
	}

	public static UserProcedure createUserProcedure(
		Long id,
		User user,
		Procedure procedure,
		LocalDateTime scheduledAt,
		Integer downtimeDays,
		LocalDate recoveryTargetDate
	) {
		UserProcedure userProcedure = UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(scheduledAt)
			.downtimeDays(downtimeDays)
			.recoveryTargetDate(recoveryTargetDate)
			.build();
		setField(userProcedure, UserProcedure.class, "id", id);
		setField(userProcedure, BaseTimeEntity.class, "createdAt", LocalDateTime.now());
		setField(userProcedure, BaseTimeEntity.class, "updatedAt", LocalDateTime.now());
		return userProcedure;
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
