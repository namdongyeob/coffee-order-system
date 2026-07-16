// 실제 Redis에서 일별 인기 메뉴 score 증가와 분리를 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.coffeeordersystem.ranking.service.PopularMenuRankingService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "ranking.consumer.enabled=false")
class PopularMenuRankingRedisIntegrationTest {

	@Autowired
	PopularMenuRankingService rankingService;

	@Autowired
	StringRedisTemplate redisTemplate;

	@BeforeEach
	void clearRankingKeys() {
		redisTemplate.delete(redisTemplate.keys("popular:menus:*"));
	}

	@Test
	void appliesSameMenuScoreTwiceOnOrderedDateForDifferentEvents() {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 9, 12, 0);

		rankingService.apply(UUID.randomUUID().toString(), "a".repeat(64), 1L, orderedAt);
		rankingService.apply(UUID.randomUUID().toString(), "b".repeat(64), 1L, orderedAt);

		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1"))
				.isEqualTo(2.0);
	}

	@Test
	void doesNotDoubleCountWhenApplyCalledTwiceWithSameEventId() {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 9, 12, 0);
		String eventId = UUID.randomUUID().toString();

		rankingService.apply(eventId, "a".repeat(64), 1L, orderedAt);
		rankingService.apply(eventId, "a".repeat(64), 1L, orderedAt);

		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1"))
				.isEqualTo(1.0);
	}

	@Test
	void sameFingerprintUsesOneAppliedEventMarkerAndIncrementsOnlyOnce() {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 9, 12, 0);
		String eventId = UUID.randomUUID().toString();
		String fingerprint = "a".repeat(64);

		rankingService.apply(eventId, fingerprint, 1L, orderedAt);
		rankingService.apply(eventId, fingerprint, 1L, orderedAt);

		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1"))
				.isEqualTo(1.0);
		assertThat(redisTemplate.opsForValue().get("ranking:applied-event:" + eventId))
				.isEqualTo(fingerprint);
		assertThat(redisTemplate.hasKey("popular:menus:processed:2026-07-09")).isFalse();
	}

	@Test
	void differentFingerprintFailsClosedWithoutChangingMarkerOrScore() {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 9, 12, 0);
		String eventId = UUID.randomUUID().toString();
		String firstFingerprint = "a".repeat(64);

		rankingService.apply(eventId, firstFingerprint, 1L, orderedAt);

		assertThatThrownBy(() -> rankingService.apply(eventId, "b".repeat(64), 2L, orderedAt.plusDays(1)))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("EVENT_ID_PAYLOAD_CONFLICT");
		assertThat(redisTemplate.opsForValue().get("ranking:applied-event:" + eventId))
				.isEqualTo(firstFingerprint);
		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1")).isEqualTo(1.0);
		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-10", "2")).isNull();
	}

	@Test
	void separatesSameMenuIntoDifferentOrderedDateKeys() {
		rankingService.apply(UUID.randomUUID().toString(), "a".repeat(64), 1L,
				LocalDateTime.of(2026, 7, 9, 23, 59));
		rankingService.apply(UUID.randomUUID().toString(), "b".repeat(64), 1L,
				LocalDateTime.of(2026, 7, 10, 0, 0));

		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1"))
				.isEqualTo(1.0);
		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-10", "1"))
				.isEqualTo(1.0);
	}

	@Test
	void separatesDifferentMenusIntoDifferentMembers() {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 9, 12, 0);

		rankingService.apply(UUID.randomUUID().toString(), "a".repeat(64), 1L, orderedAt);
		rankingService.apply(UUID.randomUUID().toString(), "b".repeat(64), 2L, orderedAt);

		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1"))
				.isEqualTo(1.0);
		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "2"))
				.isEqualTo(1.0);
	}
}
