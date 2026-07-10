// 주문 완료 이벤트 payload 계약을 검증합니다.
package com.example.coffeeordersystem.order.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderCompletedEventTest {

	@Test
	void containsDocumentedOrderCompletedPayload() {
		UUID eventId = UUID.randomUUID();
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 9, 12, 0);

		OrderCompletedEvent event = new OrderCompletedEvent(eventId, 1L, 2L, 3L, 4_500, orderedAt);

		assertThat(event.eventId()).isEqualTo(eventId);
		assertThat(event.orderId()).isEqualTo(1L);
		assertThat(event.userId()).isEqualTo(2L);
		assertThat(event.menuId()).isEqualTo(3L);
		assertThat(event.paidAmount()).isEqualTo(4_500);
		assertThat(event.orderedAt()).isEqualTo(orderedAt);
	}
}
