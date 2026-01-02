package com.sopt.cherrish.domain.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sopt.cherrish.domain.user.fixture.UserFixture;

@DisplayName("User 엔티티 단위 테스트")
class UserTest {

	@Test
	@DisplayName("User 생성 성공")
	void createUserSuccess() {
		// given & when
		User user = UserFixture.createUser("홍길동", 25);

		// then
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo("홍길동");
		assertThat(user.getAge()).isEqualTo(25);
	}

	@Test
	@DisplayName("User 업데이트 - 이름과 나이 모두 수정")
	void updateUserBothFields() {
		// given
		User user = UserFixture.createUser("홍길동", 25);

		// when
		user.update("김철수", 30);

		// then
		assertThat(user.getName()).isEqualTo("김철수");
		assertThat(user.getAge()).isEqualTo(30);
	}

	@Test
	@DisplayName("User 업데이트 - null 값은 변경하지 않음")
	void updateUserNullValues() {
		// given
		User user = UserFixture.createUser("홍길동", 25);

		// when
		user.update(null, null);

		// then
		assertThat(user.getName()).isEqualTo("홍길동");
		assertThat(user.getAge()).isEqualTo(25);
	}
}
