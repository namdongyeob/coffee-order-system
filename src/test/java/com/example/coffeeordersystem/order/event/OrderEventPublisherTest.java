// 주문 완료 Kafka producer의 topic과 key 계약을 검증합니다.
package com.example.coffeeordersystem.order.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
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

	@Test
	void logsBrokerAcknowledgementFailureWithEventContext(CapturedOutput output) {
		OrderEventPublisher publisher = new OrderEventPublisher(kafkaTemplate);
		OrderCompletedEvent event = new OrderCompletedEvent(
				UUID.randomUUID(), 1L, 7L, 2L, 4_500, LocalDateTime.of(2026, 7, 9, 12, 0));
		RuntimeException failure = new RuntimeException("broker unavailable");
		when(kafkaTemplate.send("order.completed", "7", event))
				.thenReturn(CompletableFuture.failedFuture(failure));

		publisher.publish(event);

		assertThat(output.getOut())
				.containsOnlyOnce("order_completed_event_publish_failed")
				.contains("eventId=" + event.eventId())
				.contains("orderId=1")
				.contains("userId=7")
				.contains("topic=order.completed")
				.contains("broker unavailable");
	}

	@Test
	void logsSynchronousSendFailureOnceWithoutPropagatingToCaller(CapturedOutput output) {
		OrderEventPublisher publisher = new OrderEventPublisher(kafkaTemplate);
		OrderCompletedEvent event = new OrderCompletedEvent(
				UUID.randomUUID(), 1L, 7L, 2L, 4_500, LocalDateTime.of(2026, 7, 9, 12, 0));
		when(kafkaTemplate.send("order.completed", "7", event))
				.thenThrow(new RuntimeException("producer closed"));

		assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();

		assertThat(output.getOut())
				.containsOnlyOnce("order_completed_event_publish_failed")
				.contains("eventId=" + event.eventId())
				.contains("orderId=1")
				.contains("userId=7")
				.contains("topic=order.completed")
				.contains("producer closed");
	}
}
