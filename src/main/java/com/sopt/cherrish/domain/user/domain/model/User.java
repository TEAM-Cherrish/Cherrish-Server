package com.sopt.cherrish.domain.user.domain.model;

import com.sopt.cherrish.domain.auth.domain.model.SocialProvider;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "users",
	uniqueConstraints = @UniqueConstraint(columnNames = {"social_provider", "social_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 7)
	private String name;

	@Column(nullable = false)
	private int age;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private SocialProvider socialProvider;

	@Column(nullable = false)
	private String socialId;

	private String email;

	@Builder
	private User(String name, int age, SocialProvider socialProvider, String socialId, String email) {
		this.name = name;
		this.age = age;
		this.socialProvider = socialProvider;
		this.socialId = socialId;
		this.email = email;
	}

	public void update(String name, Integer age) {
		if (name != null) {
			this.name = name;
		}
		if (age != null) {
			this.age = age;
		}
	}
}
