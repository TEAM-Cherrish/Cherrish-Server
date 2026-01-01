package com.sopt.cherrish.domain.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.user.domain.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
