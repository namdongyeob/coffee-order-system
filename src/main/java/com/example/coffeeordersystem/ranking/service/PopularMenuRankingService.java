// 주문 완료 시 일별 Redis 인기 메뉴 score를 증가시킵니다.
package com.example.coffeeordersystem.ranking.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularMenuRankingService {

	private final StringRedisTemplate redisTemplate;

	public void increment(Long menuId, LocalDateTime orderedAt) {
		PopularMenuRankingEntry entry = PopularMenuRankingEntry.from(menuId, orderedAt);
		redisTemplate.opsForZSet().incrementScore(entry.key(), entry.member(), 1);
	}
}
