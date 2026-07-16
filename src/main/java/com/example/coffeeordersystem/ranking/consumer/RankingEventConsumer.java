// Kafka 주문 완료 이벤트를 트랜잭션 처리 서비스에 전달합니다.
package com.example.coffeeordersystem.ranking.consumer;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ranking.rebuild.enabled", havingValue = "false", matchIfMissing = true)
public class RankingEventConsumer {

	private final RankingEventProcessor processor;

	@KafkaListener(
			topics = OrderEventPublisher.ORDER_COMPLETED_TOPIC,
			groupId = "ranking-consumer-group",
			autoStartup = "${ranking.consumer.enabled:true}")
	public void consume(
			OrderCompletedEvent event,
			@Header(name = RankingReplayHeaders.SOURCE, required = false) byte[] replaySource) {
		if (replaySource == null) {
			processor.process(event);
			return;
		}
		String source = new String(replaySource, StandardCharsets.UTF_8);
		if (!RankingReplayHeaders.DLT_REPLAY.equals(source)) {
			throw new IllegalStateException("지원하지 않는 ranking replay source header입니다: " + source);
		}
		processor.process(event, RankingEventSource.DLT_REPLAY);
	}
}
