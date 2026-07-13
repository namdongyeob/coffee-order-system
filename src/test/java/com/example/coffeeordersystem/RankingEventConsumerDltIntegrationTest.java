// 실제 Kafka listener의 재시도 소진과 DLT 이동 계약을 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.ranking.consumer.RankingEventProcessor;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.kafka.KafkaContainer;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RankingEventConsumerDltIntegrationTest {

	@Autowired
	KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;

	@Autowired
	KafkaContainer kafkaContainer;

	@Autowired
	KafkaListenerEndpointRegistry listenerEndpointRegistry;

	@MockitoBean
	RankingEventProcessor processor;

	@Test
	void retriesTwiceThenPublishesFailedRecordToDlt() throws Exception {
		OrderCompletedEvent event = new OrderCompletedEvent(
				UUID.randomUUID(), 11L, 6101L, 1L, 4_500,
				LocalDateTime.of(2026, 7, 12, 15, 30));
		doThrow(new IllegalStateException("forced ranking failure"))
				.when(processor).process(any(OrderCompletedEvent.class));
		listenerEndpointRegistry.getListenerContainers().forEach(
				container -> ContainerTestUtils.waitForAssignment(container, 1));

		Map<String, Object> properties = KafkaTestUtils.consumerProps(
				kafkaContainer.getBootstrapServers(), "issue-11-dlt-observer-" + UUID.randomUUID(), "false");
		try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
				properties, new StringDeserializer(), new StringDeserializer()).createConsumer()) {
			consumer.subscribe(java.util.List.of("order.completed.DLT"));
			kafkaTemplate.send(OrderEventPublisher.ORDER_COMPLETED_TOPIC, "6101", event).get();

			ConsumerRecord<String, String> dlt = KafkaTestUtils.getSingleRecord(
					consumer, "order.completed.DLT", Duration.ofSeconds(20));

			verify(processor, timeout(10_000).times(3)).process(any(OrderCompletedEvent.class));
			assertThat(dlt.key()).isEqualTo("6101");
			assertThat(dlt.partition()).isZero();
			assertThat(dlt.value()).contains(event.eventId().toString()).contains("\"menuId\":1");
			Header originalTopic = dlt.headers().lastHeader(KafkaHeaders.DLT_ORIGINAL_TOPIC);
			assertThat(originalTopic).isNotNull();
			assertThat(new String(originalTopic.value(), StandardCharsets.UTF_8))
					.isEqualTo(OrderEventPublisher.ORDER_COMPLETED_TOPIC);
		}
	}
}
