// 주문 시각과 메뉴 ID를 Redis 랭킹 key와 member로 변환합니다.
package com.example.coffeeordersystem.ranking.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

record PopularMenuRankingEntry(String key, String member) {

	private static final String KEY_PREFIX = "popular:menus:";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	static PopularMenuRankingEntry from(Long menuId, LocalDateTime orderedAt) {
		return new PopularMenuRankingEntry(
				KEY_PREFIX + orderedAt.toLocalDate().format(DATE_FORMATTER),
				menuId.toString()
		);
	}
}
