package com.sopt.cherrish.domain.user.fixture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

public class UserFixture {

	private static final String DEFAULT_NAME = "홍길동";
	private static final Integer DEFAULT_AGE = 25;
	private static final Long DEFAULT_ID = 1L;

	public static User createUser() {
		return createUser(DEFAULT_NAME, DEFAULT_AGE);
	}

	public static User createUser(String name, Integer age) {
		User user = User.builder()
			.name(name)
			.age(age)
			.build();
		setField(user, User.class, "id", DEFAULT_ID);
		setField(user, BaseTimeEntity.class, "createdAt", LocalDateTime.now());
		return user;
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
