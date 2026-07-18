package com.example.coffeeordersystem.ranking.retention;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.TestcontainersConfiguration;
import com.example.coffeeordersystem.ranking.service.PopularMenuRankingService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"ranking.consumer.enabled=false",
		"ranking.ledger.cleanup.enabled=false",
		"ranking.ledger.cleanup.redis-marker-ttl=31d"
})
class RankingMarkerRetentionIntegrationTest {

	@Autowired PopularMenuRankingService rankingService;
	@Autowired StringRedisTemplate redis;

	@Test
	void appliedEventMarkerUsesConfiguredProtectionDuration() {
		String eventId = UUID.randomUUID().toString();
		String markerKey = "ranking:applied-event:" + eventId;

		rankingService.apply(eventId, "a".repeat(64), 11L, LocalDateTime.of(2026, 7, 11, 13, 0));

		Long ttlSeconds = redis.getExpire(markerKey, TimeUnit.SECONDS);
		assertThat(ttlSeconds)
				.isPositive()
				.isBetween(Duration.ofDays(31).minusSeconds(10).toSeconds(), Duration.ofDays(31).toSeconds());
	}
}
