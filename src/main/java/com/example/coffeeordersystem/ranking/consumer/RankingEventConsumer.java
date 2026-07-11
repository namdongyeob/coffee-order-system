// Kafka 주문 완료 이벤트를 트랜잭션 처리 서비스에 전달합니다.
package com.example.coffeeordersystem.ranking.consumer;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingEventConsumer {

	private final RankingEventProcessor processor;

	@KafkaListener(topics = OrderEventPublisher.ORDER_COMPLETED_TOPIC, groupId = "ranking-consumer-group")
	public void consume(OrderCompletedEvent event) {
		processor.process(event);
	}
}
