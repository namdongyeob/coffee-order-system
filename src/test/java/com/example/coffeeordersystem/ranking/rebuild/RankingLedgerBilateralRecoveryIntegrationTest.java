// DLT replay와 Rebuild의 양방향 순서에서 ranking score 최대 1회를 실제 인프라로 검증합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.DisabledTaskSchedulerConfiguration;
import com.example.coffeeordersystem.SharedTestcontainers;
import com.example.coffeeordersystem.TestcontainersConfiguration;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.ranking.consumer.RankingEventProcessor;
import com.example.coffeeordersystem.ranking.consumer.RankingEventSource;
import com.example.coffeeordersystem.recovery.DltReplayRequest;
import com.example.coffeeordersystem.recovery.DltReplayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(properties = {
		"ranking.consumer.enabled=false",
		"ranking.rebuild.maintenance=true"
})
@Import({DisabledTaskSchedulerConfiguration.class, TestcontainersConfiguration.class})
class RankingLedgerBilateralRecoveryIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final Instant SNAPSHOT = LocalDateTime.of(2026, 7, 13, 12, 0).atZone(SEOUL).toInstant();
	private static final String DLT_TOPIC = "order.completed.DLT";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		SharedTestcontainers.start();
		registry.add("spring.kafka.bootstrap-servers", SharedTestcontainers.kafka()::getBootstrapServers);
		registry.add("spring.datasource.url", SharedTestcontainers.mysql()::getJdbcUrl);
		registry.add("spring.datasource.username", SharedTestcontainers.mysql()::getUsername);
		registry.add("spring.datasource.password", SharedTestcontainers.mysql()::getPassword);
		registry.add("spring.data.redis.host", SharedTestcontainers.redis()::getHost);
		registry.add("spring.data.redis.port", () -> SharedTestcontainers.redis().getMappedPort(6379));
		registry.add("ranking.rebuild.snapshot", () -> SNAPSHOT.toString());
	}

	@Autowired RankingRebuildService rebuildService;
	@Autowired DltReplayService dltReplayService;
	@Autowired RankingEventProcessor processor;
	@Autowired KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;
	@Autowired JdbcTemplate jdbc;
	@Autowired StringRedisTemplate redis;

	@BeforeEach
	void setUp() {
		SharedTestcontainers.clearKafkaTopics();
		redis.getConnectionFactory().getConnection().serverCommands().flushAll();
		jdbc.update("delete from processed_event");
		jdbc.update("delete from ranking_rebuild_run");
		jdbc.update("delete from ranking_event_ledger");
		jdbc.update("delete from orders");
	}

	@Test
	void dltThenRebuildCommitsSameLedgerWithoutDoubleCounting() throws Exception {
		OrderCompletedEvent event = event(UUID.randomUUID());
		insertPaidOrder(event);
		RecordMetadata dlt = publishDlt(event);

		dltReplayService.replay(request(dlt, "DLT before rebuild"));
		assertThat(jdbc.queryForObject(
				"select state from ranking_event_ledger where event_id = ?",
				String.class, event.eventId().toString()))
				.isEqualTo("RESERVED");

		rebuildService.rebuild();

		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1.0);
		assertThat(jdbc.queryForMap(
				"select state, source from ranking_event_ledger where event_id = ?", event.eventId().toString()))
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "DLT_REPLAY");
	}

	@Test
	void rebuildThenDltConsumerKeepsScoreAndRebuildLedgerSource() throws Exception {
		OrderCompletedEvent event = event(UUID.randomUUID());
		insertPaidOrder(event);
		publishOriginal(event);

		rebuildService.rebuild();
		RecordMetadata dlt = publishDlt(event);
		dltReplayService.replay(request(dlt, "rebuild before DLT"));
		processor.process(event, RankingEventSource.DLT_REPLAY);

		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1.0);
		assertThat(jdbc.queryForMap(
				"select state, source from ranking_event_ledger where event_id = ?", event.eventId().toString()))
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "REBUILD");
	}

	private OrderCompletedEvent event(UUID eventId) {
		return new OrderCompletedEvent(
				eventId, 1L, 6101L, 1L, 4_500, LocalDateTime.of(2026, 7, 12, 10, 0));
	}

	private void insertPaidOrder(OrderCompletedEvent event) {
		jdbc.update("insert into user_point(user_id, balance) values (?,?) "
					+ "on duplicate key update balance=values(balance)", event.userId(), 10_000);
		jdbc.update("insert into orders(user_id, menu_id, paid_amount, status, ordered_at) values (?,?,?,?,?)",
				event.userId(), event.menuId(), event.paidAmount(), "PAID", event.orderedAt());
	}

	private void publishOriginal(OrderCompletedEvent event) throws Exception {
		kafkaTemplate.send(OrderEventPublisher.ORDER_COMPLETED_TOPIC, event.userId().toString(), event)
				.get(10, TimeUnit.SECONDS);
		kafkaTemplate.flush();
	}

	private RecordMetadata publishDlt(OrderCompletedEvent event) throws Exception {
		ProducerRecord<String, String> record = new ProducerRecord<>(
				DLT_TOPIC, event.userId().toString(), OBJECT_MAPPER.writeValueAsString(event));
		record.headers().add(KafkaHeaders.DLT_ORIGINAL_TOPIC,
				OrderEventPublisher.ORDER_COMPLETED_TOPIC.getBytes(StandardCharsets.UTF_8));
		record.headers().add(KafkaHeaders.DLT_ORIGINAL_PARTITION, ByteBuffer.allocate(4).putInt(0).array());
		record.headers().add(KafkaHeaders.DLT_ORIGINAL_OFFSET, ByteBuffer.allocate(8).putLong(0L).array());
		try (KafkaProducer<String, String> producer = new KafkaProducer<>(Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, SharedTestcontainers.kafka().getBootstrapServers(),
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class))) {
			return producer.send(record).get(10, TimeUnit.SECONDS);
		}
	}

	private DltReplayRequest request(RecordMetadata dlt, String reason) {
		return new DltReplayRequest(DLT_TOPIC, dlt.partition(), dlt.offset(), "operator-a", reason);
	}
}
