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
			ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
					consumer, "order.completed", Duration.ofSeconds(10));

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
}
