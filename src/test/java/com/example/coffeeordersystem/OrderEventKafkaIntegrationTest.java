// 실제 Kafka에서 주문 완료 이벤트 JSON 발행 계약을 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.kafka.KafkaContainer;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OrderEventKafkaIntegrationTest {

	@Autowired
	OrderEventPublisher publisher;

	@Autowired
	KafkaContainer kafkaContainer;

	@Test
	void publishesDocumentedEventToOrderCompletedTopic() throws Exception {
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
				kafkaContainer.getBootstrapServers(), "issue-8-producer-test", "true");
		consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
				consumerProperties, new StringDeserializer(), new StringDeserializer()).createConsumer();
		consumer.subscribe(java.util.List.of("order.completed"));
		OrderCompletedEvent event = new OrderCompletedEvent(
				UUID.randomUUID(), 11L, 22L, 33L, 4_500, LocalDateTime.of(2026, 7, 9, 12, 0));

		try {
			publisher.publish(event).get(10, java.util.concurrent.TimeUnit.SECONDS);
			// 공유 topic에는 다른 테스트가 남긴 레코드가 함께 있을 수 있어 eventId로 우리 레코드를 찾는다.
			ConsumerRecord<String, String> record = pollForEventId(consumer, event.eventId(), Duration.ofSeconds(10));

			assertThat(record.key()).isEqualTo("22");
			assertThat(record.value())
					.contains("\"eventId\":\"" + event.eventId() + "\"")
					.contains("\"orderId\":11")
					.contains("\"userId\":22")
					.contains("\"menuId\":33")
					.contains("\"paidAmount\":4500")
					.contains("\"orderedAt\":\"2026-07-09T12:00:00\"");
		} finally {
			consumer.close();
		}
	}

	private ConsumerRecord<String, String> pollForEventId(Consumer<String, String> consumer, UUID eventId, Duration timeout) {
		String marker = "\"eventId\":\"" + eventId + "\"";
		long deadline = System.nanoTime() + timeout.toNanos();
		while (System.nanoTime() < deadline) {
			for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(250))) {
				if (record.value() != null && record.value().contains(marker)) {
					return record;
				}
			}
		}
		throw new IllegalStateException("eventId " + eventId + "에 대한 Kafka 레코드를 제한 시간 안에 찾지 못했습니다.");
	}
}
