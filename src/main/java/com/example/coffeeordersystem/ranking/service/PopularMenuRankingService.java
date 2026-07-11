// 주문 완료 시 일별 Redis 인기 메뉴 score를 증가시킵니다.
package com.example.coffeeordersystem.ranking.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularMenuRankingService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final String KEY_PREFIX = "popular:menus:";

	private final StringRedisTemplate redisTemplate;

	public void increment(Long menuId, LocalDateTime orderedAt) {
		PopularMenuRankingEntry entry = PopularMenuRankingEntry.from(menuId, orderedAt);
		redisTemplate.opsForZSet().incrementScore(entry.key(), entry.member(), 1);
	}

	public List<PopularMenuRanking> findRecentSevenDayRankings(LocalDate today) {
		byte[][] keys = java.util.stream.IntStream.range(0, 7)
				.mapToObj(today::minusDays)
				.map(date -> KEY_PREFIX + date.format(DATE_FORMATTER))
				.map(key -> key.getBytes(StandardCharsets.UTF_8))
				.toArray(byte[][]::new);
		Set<Tuple> tuples = redisTemplate.execute(
				(RedisCallback<Set<Tuple>>) connection -> connection.zUnionWithScores(keys));
		if (tuples == null) {
			return List.of();
		}
		return tuples.stream()
				.map(tuple -> new PopularMenuRanking(
						Long.parseLong(new String(tuple.getValue(), StandardCharsets.UTF_8)), tuple.getScore().longValue()))
				.sorted(Comparator.comparingLong(PopularMenuRanking::orderCount).reversed()
						.thenComparingLong(PopularMenuRanking::menuId))
				.toList();
	}
}
