// order.completed core payload를 ranking ledger용 SHA-256 fingerprint로 변환합니다.
package com.example.coffeeordersystem.ranking.consumer;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class RankingEventFingerprint {

	private RankingEventFingerprint() {
	}

	public static String from(OrderCompletedEvent event) {
		String canonical = String.join("|",
				Long.toString(event.orderId()), Long.toString(event.userId()), Long.toString(event.menuId()),
				Integer.toString(event.paidAmount()), event.orderedAt().toString());
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(canonical.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다", exception);
		}
	}
}
