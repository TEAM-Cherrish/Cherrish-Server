package com.sopt.cherrish.global.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Configuration
@Profile("test")
public class TestRedisConfig {

	@Bean
	@Primary
	@SuppressWarnings("unchecked")
	public RedisTemplate<String, String> redisTemplate() {
		RedisTemplate<String, String> mockTemplate = Mockito.mock(RedisTemplate.class);
		ValueOperations<String, String> mockValueOps = Mockito.mock(ValueOperations.class);

		when(mockTemplate.opsForValue()).thenReturn(mockValueOps);
		when(mockTemplate.hasKey(anyString())).thenReturn(false);
		when(mockTemplate.delete(anyString())).thenReturn(true);
		doNothing().when(mockValueOps).set(anyString(), anyString(), anyLong(), any());
		when(mockValueOps.get(anyString())).thenReturn(null);

		return mockTemplate;
	}
}
