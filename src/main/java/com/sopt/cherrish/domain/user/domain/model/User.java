package com.sopt.cherrish.domain.user.domain.model;

import com.sopt.cherrish.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private Integer age;  // 한국 나이

	// TODO: 소셜 로그인 확장성을 위한 필드 (추후 추가 예정)
	// @Column(length = 20)
	// private String provider;  // KAKAO, GOOGLE, APPLE 등

	// @Column(length = 100)
	// private String providerId;  // 소셜 로그인 제공자의 유저 ID

	// @Column(length = 100)
	// private String email;

	@Builder
	private User(String name, Integer age) {
		validateName(name);
		validateAge(age);
		this.name = name;
		this.age = age;
	}

	// 비즈니스 로직: 사용자 정보 수정 (부분 업데이트)
	public void update(String name, Integer age) {
		if (name != null) {
			validateName(name);
			this.name = name;
		}
		if (age != null) {
			validateAge(age);
			this.age = age;
		}
	}

	// 검증 로직
	private void validateName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("이름은 필수입니다");
		}
		if (name.length() > 50) {
			throw new IllegalArgumentException("이름은 50자를 초과할 수 없습니다");
		}
	}

	private void validateAge(Integer age) {
		if (age == null) {
			throw new IllegalArgumentException("나이는 필수입니다");
		}
		if (age < 1 || age > 150) {
			throw new IllegalArgumentException("나이는 1세에서 150세 사이여야 합니다");
		}
	}
}
