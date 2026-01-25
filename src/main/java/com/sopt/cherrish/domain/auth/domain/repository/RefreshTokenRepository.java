package com.sopt.cherrish.domain.auth.domain.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

	private static final String KEY_PREFIX = "refresh_token:";

	private final RedisTemplate<String, String> redisTemplate;

	public void save(Long userId, String refreshToken, long expirationMillis) {
		String key = KEY_PREFIX + userId;
		redisTemplate.opsForValue().set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
	}

	public Optional<String> findByUserId(Long userId) {
		String key = KEY_PREFIX + userId;
		String token = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(token);
	}

	public void deleteByUserId(Long userId) {
		String key = KEY_PREFIX + userId;
		redisTemplate.delete(key);
	}

	public boolean existsByUserId(Long userId) {
		String key = KEY_PREFIX + userId;
		return redisTemplate.hasKey(key);
	}
}
