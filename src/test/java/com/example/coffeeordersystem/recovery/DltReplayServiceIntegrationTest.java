// DLT 선택 재발행 계약을 실제 Kafka와 MySQL에서 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import com.example.coffeeordersystem.event.domain.ProcessedEvent;
import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.SharedTestcontainers;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.recovery.DltReplayException;
import com.example.coffeeordersystem.recovery.DltReplayPublisher;
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
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
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

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	StringRedisTemplate redis;

	@Autowired
	KafkaListenerEndpointRegistry listenerEndpointRegistry;

	@MockitoSpyBean
	DltReplayPublisher replayPublisher;

	@BeforeEach
	void resetPublisher() {
		reset(replayPublisher);
		listenerEndpointRegistry.getListenerContainers().forEach(container -> container.stop());
		SharedTestcontainers.clearKafkaTopics();
		listenerEndpointRegistry.getListenerContainers().forEach(container -> {
			container.start();
			ContainerTestUtils.waitForAssignment(container, 1);
		});
	}

	@AfterEach
	void cleanDatabase() {
		processedEvents.deleteAll();
		jdbc.update("delete from ranking_event_ledger");
		jdbc.update("delete from ranking_rebuild_run");
		redis.delete("ranking:rebuild:lock");
		redis.delete(redis.keys("popular:menus:*"));
		redis.delete(redis.keys("ranking:applied-event:*"));
	}

	@Test
	void republishesOneApprovedDltRecordWithOriginalKeyAndPayloadButWithoutDltHeaders() throws Exception {
		String eventId = UUID.randomUUID().toString();
		String payload = payload(eventId);
		RecordMetadata dlt = publishDlt("6101", payload, OrderEventPublisher.ORDER_COMPLETED_TOPIC, true, true);

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
			Header source = replayed.headers().lastHeader("ranking-replay-source");
			assertThat(source).isNotNull();
			assertThat(new String(source.value(), StandardCharsets.UTF_8)).isEqualTo("DLT_REPLAY");
		}
		awaitProcessed(eventId);
		assertThat(jdbc.queryForMap(
				"select state, source from ranking_event_ledger where event_id = ?", eventId))
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "DLT_REPLAY");
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-13", "1")).isEqualTo(1.0);
	}

	@Test
	void republishesApprovedDltRecordWithoutUsingProcessedEventAsSuccessPrecheck() throws Exception {
		String eventId = UUID.randomUUID().toString();
		processedEvents.saveAndFlush(new ProcessedEvent(
				eventId, "order.completed", "ranking-consumer-group", LocalDateTime.now()));
		RecordMetadata dlt = publishDlt("6101", payload(eventId), OrderEventPublisher.ORDER_COMPLETED_TOPIC, true, true);

		DltReplayResult result = service.replay(new DltReplayRequest(
				DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "already checked"));

		assertThat(result.status()).isEqualTo(DltReplayStatus.REPUBLISHED);
		assertThat(result.eventId()).isEqualTo(eventId);
	}

	@Test
	void pendingRebuildRunBlocksRepublishAsRetryableEvenAfterRedisLockExpired() throws Exception {
		String eventId = UUID.randomUUID().toString();
		jdbc.update("insert into ranking_rebuild_run(run_id, state, namespace, window_start_date, "
					+ "window_end_date, created_at) values (?,?,?,?,?,now(6))",
				UUID.randomUUID().toString(), "PREPARED", "pending-run", "2026-07-07", "2026-07-13");
		RecordMetadata dlt = publishDlt(
				"6101", payload(eventId), OrderEventPublisher.ORDER_COMPLETED_TOPIC, true, true);

		try (Consumer<String, String> observer = latestConsumer("pending-rebuild-observer-" + UUID.randomUUID())) {
			observer.subscribe(java.util.List.of(OrderEventPublisher.ORDER_COMPLETED_TOPIC));
			observer.poll(Duration.ofSeconds(1));

			org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.replay(new DltReplayRequest(
					DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "pending rebuild")))
					.isInstanceOf(DltReplayException.class)
					.hasMessageContaining("재시도");

			ConsumerRecords<String, String> records = observer.poll(Duration.ofSeconds(2));
			assertThat(records.records(OrderEventPublisher.ORDER_COMPLETED_TOPIC)).isEmpty();
		}
	}

	@Test
	void publishFailureKeepsReservedLedgerAndRetryCompletesWithoutDoubleCounting() throws Exception {
		String eventId = UUID.randomUUID().toString();
		RecordMetadata dlt = publishDlt(
				"6101", payload(eventId), OrderEventPublisher.ORDER_COMPLETED_TOPIC, true, true);
		doThrow(new DltReplayException("forced publish failure"))
				.when(replayPublisher).publish(any());

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.replay(new DltReplayRequest(
				DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "publish failure")))
				.isInstanceOf(DltReplayException.class)
				.hasMessageContaining("forced publish failure");
		assertThat(jdbc.queryForMap(
				"select state, source from ranking_event_ledger where event_id = ?", eventId))
				.containsEntry("state", "RESERVED")
				.containsEntry("source", "DLT_REPLAY");
		assertThat(processedEvents.existsByEventId(eventId)).isFalse();

		reset(replayPublisher);
		DltReplayResult result = service.replay(new DltReplayRequest(
				DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "publish retry"));
		assertThat(result.status()).isEqualTo(DltReplayStatus.REPUBLISHED);
		awaitProcessed(eventId);

		assertThat(jdbc.queryForObject(
				"select state from ranking_event_ledger where event_id = ?", String.class, eventId))
				.isEqualTo("COMMITTED");
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-13", "1")).isEqualTo(1.0);
	}

	@Test
	void failsClosedWhenOriginalTopicHeaderDoesNotMatchOrderCompleted() throws Exception {
		RecordMetadata dlt = publishDlt("6101", payload(UUID.randomUUID().toString()), "other.topic", true, true);

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.replay(new DltReplayRequest(
				DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "header validation")))
				.isInstanceOf(DltReplayException.class)
				.hasMessageContaining("original topic");
	}

	@Test
	void failsClosedWhenOriginalOffsetHeaderIsMissing() throws Exception {
		RecordMetadata dlt = publishDlt("6101", payload(UUID.randomUUID().toString()),
				OrderEventPublisher.ORDER_COMPLETED_TOPIC, true, false);

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.replay(new DltReplayRequest(
				DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", "header validation")))
				.isInstanceOf(DltReplayException.class)
				.hasMessageContaining("원본 offset");
	}

	@Test
	void dltDifferentFingerprintFailsClosedBeforeRepublishAndKeepsScore() throws Exception {
		String eventId = UUID.randomUUID().toString();
		RecordMetadata first = publishDlt(
				"6101", payload(eventId), OrderEventPublisher.ORDER_COMPLETED_TOPIC, true, true);
		service.replay(new DltReplayRequest(
				DLT_TOPIC, first.partition(), first.offset(), "operator-a", "initial DLT"));
		awaitProcessed(eventId);
		String fingerprint = jdbc.queryForObject(
				"select payload_fingerprint from ranking_event_ledger where event_id = ?", String.class, eventId);

		String conflictingPayload = payload(eventId).replace("\"menuId\":1", "\"menuId\":2");
		RecordMetadata conflicting = publishDlt(
				"6101", conflictingPayload, OrderEventPublisher.ORDER_COMPLETED_TOPIC, true, true);

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.replay(new DltReplayRequest(
				DLT_TOPIC, conflicting.partition(), conflicting.offset(), "operator-a", "conflict DLT")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("EVENT_ID_PAYLOAD_CONFLICT");
		assertThat(jdbc.queryForMap(
				"select state, source, payload_fingerprint from ranking_event_ledger where event_id = ?", eventId))
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "DLT_REPLAY")
				.containsEntry("payload_fingerprint", fingerprint);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-13", "1")).isEqualTo(1.0);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-13", "2")).isNull();
	}

	private RecordMetadata publishDlt(
			String key, String value, String originalTopic, boolean includeOriginalPartition, boolean includeOriginalOffset)
			throws Exception {
		ProducerRecord<String, String> record = new ProducerRecord<>(DLT_TOPIC, key, value);
		record.headers().add(KafkaHeaders.DLT_ORIGINAL_TOPIC, originalTopic.getBytes(StandardCharsets.UTF_8));
		if (includeOriginalPartition) {
			record.headers().add(KafkaHeaders.DLT_ORIGINAL_PARTITION, ByteBuffer.allocate(4).putInt(0).array());
		}
		if (includeOriginalOffset) {
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

	private Consumer<String, String> latestConsumer(String groupId) {
		Map<String, Object> properties = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), groupId, "false");
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
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
