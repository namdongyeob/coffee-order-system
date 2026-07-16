// 주문 완료 시 일별 Redis 인기 메뉴 score를 증가시킵니다.
package com.example.coffeeordersystem.ranking.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularMenuRankingService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final String KEY_PREFIX = "popular:menus:";
	private static final String APPLIED_EVENT_KEY_PREFIX = "ranking:applied-event:";
	private static final Duration APPLIED_EVENT_TTL = Duration.ofDays(30);

	private static final DefaultRedisScript<Long> APPLY_FINGERPRINT_ONCE_SCRIPT = new DefaultRedisScript<>(
			"local existing = redis.call('GET', KEYS[2]) "
					+ "if existing then if existing == ARGV[2] then return 0 else return -1 end end "
					+ "redis.call('SET', KEYS[2], ARGV[2], 'EX', ARGV[3]) "
					+ "redis.call('ZINCRBY', KEYS[1], 1, ARGV[1]) "
					+ "return 1",
			Long.class);

	private final StringRedisTemplate redisTemplate;

	public void apply(String eventId, String fingerprint, Long menuId, LocalDateTime orderedAt) {
		PopularMenuRankingEntry entry = PopularMenuRankingEntry.from(menuId, orderedAt);
		Long result = redisTemplate.execute(
				APPLY_FINGERPRINT_ONCE_SCRIPT,
				List.of(entry.key(), APPLIED_EVENT_KEY_PREFIX + eventId),
				entry.member(), fingerprint, Long.toString(APPLIED_EVENT_TTL.toSeconds()));
		if (Long.valueOf(-1L).equals(result)) {
			throw new IllegalStateException("EVENT_ID_PAYLOAD_CONFLICT eventId=" + eventId);
		}
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
