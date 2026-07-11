// 실제 Redis에서 일별 인기 메뉴 score 증가와 분리를 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.ranking.service.PopularMenuRankingService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
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
	void incrementsSameMenuScoreTwiceOnOrderedDate() {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 9, 12, 0);

		rankingService.increment(1L, orderedAt);
		rankingService.increment(1L, orderedAt);

		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1"))
				.isEqualTo(2.0);
	}

	@Test
	void separatesSameMenuIntoDifferentOrderedDateKeys() {
		rankingService.increment(1L, LocalDateTime.of(2026, 7, 9, 23, 59));
		rankingService.increment(1L, LocalDateTime.of(2026, 7, 10, 0, 0));

		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1"))
				.isEqualTo(1.0);
		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-10", "1"))
				.isEqualTo(1.0);
	}

	@Test
	void separatesDifferentMenusIntoDifferentMembers() {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 9, 12, 0);

		rankingService.increment(1L, orderedAt);
		rankingService.increment(2L, orderedAt);

		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1"))
				.isEqualTo(1.0);
		assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "2"))
				.isEqualTo(1.0);
	}
}
