// DLT 선택 재발행 계약을 실제 Kafka와 MySQL에서 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.event.domain.ProcessedEvent;
import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.recovery.DltReplayException;
import com.example.coffeeordersystem.recovery.DltReplayRequest;
import com.example.coffeeordersystem.recovery.DltReplayResult;
import com.example.coffeeordersystem.recovery.DltReplayService;
import com.example.coffeeordersystem.recovery.DltReplayStatus;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.kafka.KafkaContainer;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DltReplayServiceIntegrationTest {

	private static final String DLT_TOPIC = "order.completed.DLT";

	@Autowired
	DltReplayService service;

	@Autowired
	KafkaContainer kafka;

	@Autowired
	ProcessedEventRepository processedEvents;

	@AfterEach
	void cleanDatabase() {
		processedEvents.deleteAll();
	}

	@Test
	void republishesOneApprovedDltRecordWithOriginalKeyAndPayloadButWithoutDltHeaders() throws Exception {
		String eventId = UUID.randomUUID().toString();
		String payload = payload(eventId);
		RecordMetadata dlt = publishDlt("6101", payload, OrderEventPublisher.ORDER_COMPLETED_TOPIC, true);

		DltReplayResult result = service.replay(new DltReplayRequest(
				DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "Redis recovered"));

		assertThat(result.status()).isEqualTo(DltReplayStatus.REPUBLISHED);
		assertThat(result.eventId()).isEqualTo(eventId);
		try (Consumer<String, String> consumer = consumer("replay-observer-" + UUID.randomUUID())) {
			consumer.subscribe(java.util.List.of(OrderEventPublisher.ORDER_COMPLETED_TOPIC));
			ConsumerRecord<String, String> replayed = KafkaTestUtils.getSingleRecord(
					consumer, OrderEventPublisher.ORDER_COMPLETED_TOPIC, Duration.ofSeconds(10));
			assertThat(replayed.key()).isEqualTo("6101");
			assertThat(replayed.value()).isEqualTo(payload);
			assertThat(replayed.headers().headers(KafkaHeaders.DLT_ORIGINAL_TOPIC)).isEmpty();
			assertThat(replayed.headers().headers(KafkaHeaders.DLT_EXCEPTION_FQCN)).isEmpty();
		}
		awaitProcessed(eventId);
	}

	@Test
	void skipsApprovedDltRecordWhenEventWasAlreadyProcessed() throws Exception {
		String eventId = UUID.randomUUID().toString();
		processedEvents.saveAndFlush(new ProcessedEvent(
				eventId, "order.completed", "ranking-consumer-group", LocalDateTime.now()));
		RecordMetadata dlt = publishDlt("6101", payload(eventId), OrderEventPublisher.ORDER_COMPLETED_TOPIC, true);

		DltReplayResult result = service.replay(new DltReplayRequest(
				DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "already checked"));

		assertThat(result.status()).isEqualTo(DltReplayStatus.SKIPPED_ALREADY_PROCESSED);
		assertThat(result.eventId()).isEqualTo(eventId);
	}

	@Test
	void failsClosedWhenOriginalTopicHeaderDoesNotMatchOrderCompleted() throws Exception {
		RecordMetadata dlt = publishDlt("6101", payload(UUID.randomUUID().toString()), "other.topic", true);

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.replay(new DltReplayRequest(
				DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "header validation")))
				.isInstanceOf(DltReplayException.class)
				.hasMessageContaining("original topic");
	}

	@Test
	void failsClosedWhenOriginalIdentificationHeaderIsMissing() throws Exception {
		RecordMetadata dlt = publishDlt("6101", payload(UUID.randomUUID().toString()),
				OrderEventPublisher.ORDER_COMPLETED_TOPIC, false);

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.replay(new DltReplayRequest(
				DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "header validation")))
				.isInstanceOf(DltReplayException.class)
				.hasMessageContaining("원본 partition");
	}

	private RecordMetadata publishDlt(String key, String value, String originalTopic, boolean includeIdentifiers)
			throws Exception {
		ProducerRecord<String, String> record = new ProducerRecord<>(DLT_TOPIC, key, value);
		record.headers().add(KafkaHeaders.DLT_ORIGINAL_TOPIC, originalTopic.getBytes(StandardCharsets.UTF_8));
		if (includeIdentifiers) {
			record.headers().add(KafkaHeaders.DLT_ORIGINAL_PARTITION, ByteBuffer.allocate(4).putInt(0).array());
			record.headers().add(KafkaHeaders.DLT_ORIGINAL_OFFSET, ByteBuffer.allocate(8).putLong(0L).array());
		}
		record.headers().add(KafkaHeaders.DLT_EXCEPTION_FQCN,
				"java.lang.IllegalStateException".getBytes(StandardCharsets.UTF_8));
		try (KafkaProducer<String, String> producer = producer()) {
			return producer.send(record).get();
		}
	}

	private KafkaProducer<String, String> producer() {
		Map<String, Object> properties = Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return new KafkaProducer<>(properties);
	}

	private Consumer<String, String> consumer(String groupId) {
		Map<String, Object> properties = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), groupId, "false");
		return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), new StringDeserializer())
				.createConsumer();
	}

	private String payload(String eventId) {
		return "{\"eventId\":\"" + eventId
				+ "\",\"orderId\":11,\"userId\":6101,\"menuId\":1,\"paidAmount\":4500,\"orderedAt\":\"2026-07-13T17:10:00\"}";
	}

	private void awaitProcessed(String eventId) throws InterruptedException {
		long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
		while (System.nanoTime() < deadline) {
			if (processedEvents.existsByEventId(eventId)) {
				return;
			}
			Thread.sleep(100);
		}
		throw new AssertionError("재발행한 event가 ranking consumer에서 처리되지 않았습니다.");
	}
}
