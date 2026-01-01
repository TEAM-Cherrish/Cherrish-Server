package com.sopt.cherrish.domain.user.fixture;

import com.sopt.cherrish.domain.user.domain.model.User;

public class UserFixture {

	private static final String DEFAULT_NAME = "홍길동";
	private static final Integer DEFAULT_AGE = 25;

	public static User createUser() {
		return createUser(DEFAULT_NAME, DEFAULT_AGE);
	}

	public static User createUser(String name, Integer age) {
		return User.builder()
			.name(name)
			.age(age)
			.build();
	}
}
