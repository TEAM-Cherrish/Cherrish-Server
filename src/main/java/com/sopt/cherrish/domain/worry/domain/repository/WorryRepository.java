package com.sopt.cherrish.domain.worry.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sopt.cherrish.domain.worry.domain.model.Worry;

public interface WorryRepository extends JpaRepository<Worry, Long> {
}
