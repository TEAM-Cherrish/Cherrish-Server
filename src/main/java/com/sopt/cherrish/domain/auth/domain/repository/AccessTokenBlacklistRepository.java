package com.sopt.cherrish.domain.auth.domain.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Access Token 블랙리스트를 Redis에 관리하는 저장소.
 *
 * <p>로그아웃된 Access Token을 저장하여 만료 전까지 재사용을 방지합니다.
 * TTL은 토큰의 남은 만료 시간으로 설정되어 자동으로 정리됩니다.</p>
 */
@Repository
@RequiredArgsConstructor
public class AccessTokenBlacklistRepository {

	private static final String KEY_PREFIX = "blacklist:";

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * Access Token을 블랙리스트에 추가합니다.
	 *
	 * @param token 블랙리스트에 추가할 Access Token
	 * @param expirationMillis 토큰의 남은 만료 시간 (밀리초)
	 */
	public void add(String token, long expirationMillis) {
		if (expirationMillis <= 0) {
			return;
		}
		String key = KEY_PREFIX + token;
		redisTemplate.opsForValue().set(key, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
	}

	/**
	 * Access Token이 블랙리스트에 있는지 확인합니다.
	 *
	 * @param token 확인할 Access Token
	 * @return 블랙리스트에 있으면 true
	 */
	public boolean isBlacklisted(String token) {
		String key = KEY_PREFIX + token;
		return redisTemplate.hasKey(key);
	}
}
