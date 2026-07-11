// 인기 메뉴 Redis key와 member 변환 규칙을 검증합니다.
package com.example.coffeeordersystem.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PopularMenuRankingEntryTest {

	@Test
	void createsDailyKeyFromOrderedAtDate() {
		PopularMenuRankingEntry entry = PopularMenuRankingEntry.from(
				1L,
				LocalDateTime.of(2026, 7, 9, 12, 0)
		);

		assertThat(entry.key()).isEqualTo("popular:menus:2026-07-09");
	}

	@Test
	void convertsMenuIdToStringMember() {
		PopularMenuRankingEntry entry = PopularMenuRankingEntry.from(
				42L,
				LocalDateTime.of(2026, 7, 9, 12, 0)
		);

		assertThat(entry.member()).isEqualTo("42");
	}
}
