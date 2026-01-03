package com.sopt.cherrish.domain.worry.fixture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.sopt.cherrish.domain.worry.domain.model.Worry;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

public class WorryFixture {

	private static final String DEFAULT_CONTENT = "여드름/트러블";
	private static final Long DEFAULT_ID = 1L;

	private WorryFixture() {
		// Utility class
	}

	public static Worry createWorry() {
		return createWorry(DEFAULT_ID, DEFAULT_CONTENT);
	}

	public static Worry createWorry(String content) {
		return createWorry(DEFAULT_ID, content);
	}

	public static Worry createWorry(Long id, String content) {
		Worry worry = Worry.builder()
			.content(content)
			.build();
		setField(worry, Worry.class, "id", id);
		setField(worry, BaseTimeEntity.class, "createdAt", LocalDateTime.now());
		setField(worry, BaseTimeEntity.class, "updatedAt", LocalDateTime.now());
		return worry;
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
