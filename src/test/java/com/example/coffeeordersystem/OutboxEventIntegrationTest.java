// 주문 완료 후 OutboxEvent가 publishPending()으로 실제 Kafka에 전달되고 발행 상태가 갱신되는지 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.order.dto.OrderResponse;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.order.event.OutboxEvent;
import com.example.coffeeordersystem.order.event.OutboxEventPublisher;
import com.example.coffeeordersystem.order.event.OutboxEventRepository;
import com.example.coffeeordersystem.order.service.OrderService;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.kafka.KafkaContainer;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OutboxEventIntegrationTest {

	@Autowired
	OrderService orderService;

	@Autowired
	UserPointRepository userPointRepository;

	@Autowired
	OutboxEventRepository outboxEventRepository;

	@Autowired
	OutboxEventPublisher outboxEventPublisher;

	@Autowired
	KafkaContainer kafkaContainer;

	@BeforeEach
	void setUp() {
		outboxEventRepository.deleteAll();
	}

	@Test
	void createOrderSavesOutboxEventInOrderTransaction() {
		userPointRepository.save(new UserPoint(201L, 10_000));

		OrderResponse response = orderService.createOrder(201L, 1L);

		List<OutboxEvent> events = outboxEventRepository.findAll();
		assertThat(events).hasSize(1);
		assertThat(events.get(0).getEventType()).isEqualTo(OrderEventPublisher.ORDER_COMPLETED_TOPIC);
		assertThat(events.get(0).getPayload())
				.contains("\"orderId\":" + response.orderId())
				.contains("\"userId\":201")
				.contains("\"paidAmount\":4500");
	}

	@Test
	void publishPendingDeliversOutboxEventToKafkaAndMarksItPublished() throws Exception {
		userPointRepository.save(new UserPoint(202L, 10_000));
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
				kafkaContainer.getBootstrapServers(), "issue-99-outbox-test", "true");
		consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
				consumerProperties, new StringDeserializer(), new StringDeserializer()).createConsumer();
		consumer.subscribe(List.of(OrderEventPublisher.ORDER_COMPLETED_TOPIC));

		try {
			OrderResponse response = orderService.createOrder(202L, 1L);
			OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);

			outboxEventPublisher.publishPending();

			assertThat(outboxEventRepository.findById(outboxEvent.getId()))
					.get()
					.extracting(OutboxEvent::getPublishedAt)
					.isNotNull();
			// 공유 topic에는 다른 테스트가 남긴 레코드가 함께 있을 수 있어 orderId로 우리 레코드를 찾는다.
			ConsumerRecord<String, String> record = pollForOrderId(consumer, response.orderId(), Duration.ofSeconds(10));
			assertThat(record.value())
					.contains("\"orderId\":" + response.orderId())
					.contains("\"userId\":202");
		} finally {
			consumer.close();
		}
	}

	private ConsumerRecord<String, String> pollForOrderId(Consumer<String, String> consumer, Long orderId, Duration timeout) {
		String marker = "\"orderId\":" + orderId;
		long deadline = System.nanoTime() + timeout.toNanos();
		while (System.nanoTime() < deadline) {
			for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(250))) {
				if (record.value() != null && record.value().contains(marker)) {
					return record;
				}
			}
		}
		throw new IllegalStateException("orderId " + orderId + "에 대한 Kafka 레코드를 제한 시간 안에 찾지 못했습니다.");
	}
}
