package com.sopt.cherrish.domain.user.fixture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.sopt.cherrish.domain.auth.domain.model.SocialProvider;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

public class UserFixture {

	private static final String DEFAULT_NAME = "홍길동";
	private static final int DEFAULT_AGE = 25;
	private static final Long DEFAULT_ID = 1L;
	private static final SocialProvider DEFAULT_SOCIAL_PROVIDER = SocialProvider.KAKAO;
	private static final String DEFAULT_SOCIAL_ID = "test_social_id_12345";

	private UserFixture() {
		// Utility class
	}

	public static User createUser() {
		return createUser(DEFAULT_NAME, DEFAULT_AGE);
	}

	public static User createUser(String name, int age) {
		User user = User.builder()
			.name(name)
			.age(age)
			.socialProvider(DEFAULT_SOCIAL_PROVIDER)
			.socialId(DEFAULT_SOCIAL_ID)
			.build();
		setField(user, User.class, "id", DEFAULT_ID);
		setField(user, BaseTimeEntity.class, "createdAt", LocalDateTime.now());
		return user;
	}

	public static User createUser(String name, int age, LocalDateTime createdAt) {
		User user = User.builder()
			.name(name)
			.age(age)
			.socialProvider(DEFAULT_SOCIAL_PROVIDER)
			.socialId(DEFAULT_SOCIAL_ID)
			.build();
		setField(user, User.class, "id", DEFAULT_ID);
		setField(user, BaseTimeEntity.class, "createdAt", createdAt);
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
