package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.menu.dto.PopularMenuResponse;
import com.example.coffeeordersystem.menu.service.MenuService;
import com.example.coffeeordersystem.ranking.service.PopularMenuRankingService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "ranking.consumer.enabled=false")
class PopularMenuRedisIntegrationTest {

	@Autowired
	StringRedisTemplate redisTemplate;

	@Autowired
	PopularMenuRankingService rankingService;

	@Autowired
	MenuService menuService;

	@BeforeEach
	void clearRankingKeys() {
		redisTemplate.delete(redisTemplate.keys("popular:menus:*"));
	}

	@Test
	void aggregatesSevenDailyKeysAndOrdersTiesByNumericMenuIdWithoutCreatingTemporaryKey() {
		LocalDate today = LocalDate.now();
		add(today, "2", 2);
		add(today.minusDays(1), "2", 3);
		add(today, "10", 5);
		add(today.minusDays(7), "1", 100);
		var keysBefore = redisTemplate.keys("*");

		var rankings = rankingService.findRecentSevenDayRankings(today);

		assertThat(rankings).extracting(ranking -> ranking.menuId()).containsExactly(2L, 10L);
		assertThat(rankings).extracting(ranking -> ranking.orderCount()).containsExactly(5L, 5L);
		assertThat(redisTemplate.keys("*")).containsExactlyInAnyOrderElementsOf(keysBefore);
	}

	@Test
	void skipsDeletedRedisMemberAndFillsTopThreeFromRemainingMenus() {
		LocalDate today = LocalDate.now();
		add(today, "999", 10);
		add(today, "1", 9);
		add(today, "2", 8);
		add(today, "3", 7);
		add(today, "4", 6);

		List<PopularMenuResponse> menus = menuService.getPopularMenus();

		assertThat(menus).extracting(PopularMenuResponse::rank).containsExactly(1, 2, 3);
		assertThat(menus).extracting(PopularMenuResponse::menuId).containsExactly(1L, 2L, 3L);
	}

	private void add(LocalDate date, String member, double score) {
		redisTemplate.opsForZSet().add("popular:menus:" + date, member, score);
	}
}
