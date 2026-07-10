// 주문 완료 이벤트를 문서 계약의 Kafka topic으로 발행합니다.
package com.example.coffeeordersystem.order.event;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

	public static final String ORDER_COMPLETED_TOPIC = "order.completed";

	private final KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;

	public CompletableFuture<SendResult<String, OrderCompletedEvent>> publish(OrderCompletedEvent event) {
		return kafkaTemplate.send(ORDER_COMPLETED_TOPIC, event.userId().toString(), event);
	}
}
