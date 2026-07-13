// maintenance mode와 rebuild 실행 잠금의 선행 조건을 검증합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

class RankingRebuildServiceTest {

	@Test
	void selectsOnlyDateKeysIntersectingHalfOpenWindow() {
		assertThat(RankingRebuildService.datesIntersecting(
				LocalDateTime.of(2026, 7, 6, 0, 0),
				LocalDateTime.of(2026, 7, 13, 0, 0)))
				.containsExactly("2026-07-06", "2026-07-07", "2026-07-08", "2026-07-09",
						"2026-07-10", "2026-07-11", "2026-07-12");
	}

	@Test
	void rejectsExecutionOutsideMaintenanceModeBeforeInfrastructureAccess() {
		RankingRebuildService service = new RankingRebuildService(
				mock(StringRedisTemplate.class),
				mock(JdbcTemplate.class),
				"localhost:9092",
				false,
				"");

		assertThatThrownBy(service::rebuild)
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("maintenance mode");
	}
}
