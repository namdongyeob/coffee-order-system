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
	private static final String PROCESSED_KEY_PREFIX = "popular:menus:processed:";
	private static final Duration PROCESSED_KEY_TTL = Duration.ofDays(9);

	// eventId가 이미 반영됐는지 Redis에서 원자적으로 확인한 뒤에만 ZINCRBY한다.
	// Kafka 재처리로 같은 eventId가 다시 들어와도(예: DB 커밋 실패 후 재시도) score가 중복 증가하지 않는다.
	private static final DefaultRedisScript<Long> INCREMENT_ONCE_SCRIPT = new DefaultRedisScript<>(
			"if redis.call('SISMEMBER', KEYS[2], ARGV[2]) == 1 then "
					+ "return 0 "
					+ "end "
					+ "redis.call('SADD', KEYS[2], ARGV[2]) "
					+ "redis.call('EXPIRE', KEYS[2], ARGV[3]) "
					+ "redis.call('ZINCRBY', KEYS[1], 1, ARGV[1]) "
					+ "return 1",
			Long.class);

	private final StringRedisTemplate redisTemplate;

	public void increment(String eventId, Long menuId, LocalDateTime orderedAt) {
		PopularMenuRankingEntry entry = PopularMenuRankingEntry.from(menuId, orderedAt);
		String processedKey = PROCESSED_KEY_PREFIX + orderedAt.toLocalDate().format(DATE_FORMATTER);
		redisTemplate.execute(
				INCREMENT_ONCE_SCRIPT,
				List.of(entry.key(), processedKey),
				entry.member(), eventId, Long.toString(PROCESSED_KEY_TTL.toSeconds()));
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
