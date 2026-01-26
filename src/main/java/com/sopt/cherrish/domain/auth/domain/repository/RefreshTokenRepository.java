package com.sopt.cherrish.domain.auth.domain.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Refresh Token을 Redis에 저장하고 관리하는 저장소.
 *
 * <p>사용자별로 하나의 Refresh Token만 유효하도록 관리합니다.
 * TTL이 설정되어 만료된 토큰은 자동으로 삭제됩니다.</p>
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

	private static final String KEY_PREFIX = "refresh_token:";

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * Refresh Token을 저장합니다.
	 *
	 * <p>기존에 저장된 토큰이 있으면 덮어씁니다.
	 * 만료 시간이 0 이하인 경우 저장하지 않습니다.</p>
	 *
	 * @param userId 사용자 ID
	 * @param refreshToken 저장할 Refresh Token
	 * @param expirationMillis 만료 시간 (밀리초)
	 */
	public void save(Long userId, String refreshToken, long expirationMillis) {
		if (expirationMillis <= 0) {
			return;
		}
		String key = KEY_PREFIX + userId;
		redisTemplate.opsForValue().set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
	}

	/**
	 * 사용자의 Refresh Token을 조회합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 저장된 Refresh Token, 없으면 빈 Optional
	 */
	public Optional<String> findByUserId(Long userId) {
		String key = KEY_PREFIX + userId;
		String token = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(token);
	}

	/**
	 * 사용자의 Refresh Token을 삭제합니다.
	 *
	 * <p>로그아웃 시 호출되어 해당 토큰을 무효화합니다.</p>
	 *
	 * @param userId 사용자 ID
	 */
	public void deleteByUserId(Long userId) {
		String key = KEY_PREFIX + userId;
		redisTemplate.delete(key);
	}

	/**
	 * 사용자의 Refresh Token 존재 여부를 확인합니다.
	 *
	 * <p>Redis 연결 문제 등으로 null이 반환될 수 있으므로 null-safe 비교를 수행합니다.</p>
	 *
	 * @param userId 사용자 ID
	 * @return 토큰이 존재하면 true, 없거나 확인 불가 시 false
	 */
	public boolean existsByUserId(Long userId) {
		String key = KEY_PREFIX + userId;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}
}
