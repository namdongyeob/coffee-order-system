// 주문 완료 Kafka producer의 topic과 key 계약을 검증합니다.
package com.example.coffeeordersystem.order.event;

import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

	@Mock
	KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;

	@Test
	void publishesOrderCompletedEventWithUserIdKey() {
		OrderEventPublisher publisher = new OrderEventPublisher(kafkaTemplate);
		OrderCompletedEvent event = new OrderCompletedEvent(
				UUID.randomUUID(), 1L, 7L, 2L, 4_500, LocalDateTime.of(2026, 7, 9, 12, 0));

		publisher.publish(event);

		verify(kafkaTemplate).send("order.completed", "7", event);
	}
}
