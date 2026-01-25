package com.sopt.cherrish.domain.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.auth.domain.model.SocialProvider;
import com.sopt.cherrish.domain.user.domain.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

	boolean existsBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);
}
