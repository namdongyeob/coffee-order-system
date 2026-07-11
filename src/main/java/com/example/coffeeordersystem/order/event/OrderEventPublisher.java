// 주문 완료 이벤트를 문서 계약의 Kafka topic으로 발행합니다.
package com.example.coffeeordersystem.order.event;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

	public static final String ORDER_COMPLETED_TOPIC = "order.completed";

	private final KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;

	public CompletableFuture<SendResult<String, OrderCompletedEvent>> publish(OrderCompletedEvent event) {
		CompletableFuture<SendResult<String, OrderCompletedEvent>> result;
		try {
			result = kafkaTemplate.send(ORDER_COMPLETED_TOPIC, event.userId().toString(), event);
		} catch (RuntimeException failure) {
			logFailure(event, failure);
			return CompletableFuture.failedFuture(failure);
		}
		result.whenComplete((sendResult, failure) -> {
			if (failure != null) {
				logFailure(event, failure);
			}
		});
		return result;
	}

	private void logFailure(OrderCompletedEvent event, Throwable failure) {
		log.error(
				"order_completed_event_publish_failed eventId={} orderId={} userId={} topic={}",
				event.eventId(), event.orderId(), event.userId(), ORDER_COMPLETED_TOPIC, failure
		);
	}
}
