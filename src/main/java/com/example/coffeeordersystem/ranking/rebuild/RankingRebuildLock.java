// Redis token lock의 소유권을 획득·갱신·해제합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
class RankingRebuildLock {

	static final String KEY = "ranking:rebuild:lock";
	private static final Duration LEASE = Duration.ofMinutes(30);

	private final StringRedisTemplate redis;

	RankingRebuildLock(StringRedisTemplate redis) {
		this.redis = redis;
	}

	boolean acquire(String token) {
		return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(KEY, token, LEASE));
	}

	boolean renew(String token) {
		Long renewed = redis.execute(new DefaultRedisScript<>(
				"if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('PEXPIRE',KEYS[1],ARGV[2]) else return 0 end",
				Long.class), List.of(KEY), token, Long.toString(LEASE.toMillis()));
		return Long.valueOf(1L).equals(renewed);
	}

	void release(String token) {
		redis.execute(new DefaultRedisScript<>(
				"if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('DEL',KEYS[1]) else return 0 end",
				Long.class), List.of(KEY), token);
	}
}
