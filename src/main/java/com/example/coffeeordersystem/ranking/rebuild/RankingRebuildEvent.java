// rebuild ledger에 기록할 order.completed payload의 정규화된 값입니다.
package com.example.coffeeordersystem.ranking.rebuild;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

record RankingRebuildEvent(
		UUID eventId,
		Long orderId,
		Long userId,
		Long menuId,
		int paidAmount,
		LocalDateTime orderedAt) {

	static RankingRebuildEvent from(OrderCompletedEvent event) {
		return new RankingRebuildEvent(
				event.eventId(), event.orderId(), event.userId(), event.menuId(), event.paidAmount(), event.orderedAt());
	}

	String payloadFingerprint() {
		String canonical = String.join("|",
				Long.toString(orderId), Long.toString(userId), Long.toString(menuId),
				Integer.toString(paidAmount), orderedAt.toString());
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(canonical.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다", exception);
		}
	}
}
