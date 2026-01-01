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
		setId(user, DEFAULT_ID);
		setCreatedAt(user, LocalDateTime.now());
		return user;
	}

	private static void setId(User user, Long id) {
		try {
			Field idField = User.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(user, id);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("Failed to set id field", e);
		}
	}

	private static void setCreatedAt(User user, LocalDateTime createdAt) {
		try {
			Field createdAtField = BaseTimeEntity.class.getDeclaredField("createdAt");
			createdAtField.setAccessible(true);
			createdAtField.set(user, createdAt);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("Failed to set createdAt field", e);
		}
	}
}
