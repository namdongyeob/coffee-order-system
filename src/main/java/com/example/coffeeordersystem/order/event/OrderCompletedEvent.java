// 주문 완료 후 Kafka로 전달할 이벤트 계약입니다.
package com.example.coffeeordersystem.order.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCompletedEvent(
		UUID eventId,
		Long orderId,
		Long userId,
		Long menuId,
		int paidAmount,
		@JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime orderedAt
) {
}
