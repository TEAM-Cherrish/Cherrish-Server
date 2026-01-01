package com.sopt.cherrish.domain.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.user.domain.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	// JPA 메서드 네이밍 규칙 사용
	boolean existsByName(String name);

	Optional<User> findByName(String name);

	// TODO: 소셜 로그인 확장 시 추가 예정
	// Optional<User> findByProviderAndProviderId(String provider, String providerId);
	// boolean existsByEmail(String email);
}
