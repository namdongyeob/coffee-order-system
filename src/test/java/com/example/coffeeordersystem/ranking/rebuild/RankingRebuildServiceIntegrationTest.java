// Kafka replay 결과를 MySQL 원천과 비교해 Redis에 원자 반영하는 복구 계약을 검증합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(properties = {
		"ranking.consumer.enabled=false",
		"ranking.rebuild.maintenance=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RankingRebuildServiceIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final Instant SNAPSHOT = LocalDateTime.of(2026, 7, 13, 12, 0).atZone(SEOUL).toInstant();

	@Container
	static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.9.1"));

	@Container
	static final MySQLContainer MYSQL = new MySQLContainer(DockerImageName.parse("mysql:8.4.5"));

	@Container
	static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4.2"))
			.withExposedPorts(6379);

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
		registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
		registry.add("spring.datasource.username", MYSQL::getUsername);
		registry.add("spring.datasource.password", MYSQL::getPassword);
		registry.add("spring.data.redis.host", REDIS::getHost);
		registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
		registry.add("ranking.rebuild.snapshot", () -> SNAPSHOT.toString());
	}

	@Autowired RankingRebuildService service;
	@Autowired KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;
	@Autowired StringRedisTemplate redis;
	@Autowired JdbcTemplate jdbc;
	@Autowired ProducerFactory<String, OrderCompletedEvent> producerFactory;

	@BeforeEach
	void clean() {
		redis.getConnectionFactory().getConnection().serverCommands().flushAll();
		jdbc.update("delete from processed_event");
		jdbc.update("delete from orders");
	}

	@Test
	@Order(4)
	void rebuildsThroughActualKafkaMysqlAndRedisThenMovesNormalGroupOffset() throws Exception {
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		insertPaidOrder(2L, LocalDateTime.of(2026, 7, 6, 12, 0));
		publish(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		publish(2L, LocalDateTime.of(2026, 7, 6, 12, 0));
		publish(3L, LocalDateTime.of(2026, 7, 13, 12, 0));
		redis.opsForZSet().add("popular:menus:2026-07-12", "999", 99);

		RankingRebuildResult result = service.rebuild();

		assertThat(result.counts()).containsExactlyInAnyOrderEntriesOf(Map.of(
				new RankingRebuildCount("2026-07-12", 1L), 1L,
				new RankingRebuildCount("2026-07-06", 2L), 1L));
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-06", "2")).isEqualTo(1);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-13", "3")).isNull();
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "999")).isNull();
		assertThat(redis.keys("rebuild:popular:menus:*")).isEmpty();
		assertThat(redis.keys("rebuild:backup:popular:menus:*")).isEmpty();
		assertThat(jdbc.queryForObject("select count(*) from processed_event", Long.class)).isZero();

		RankingRebuildResult repeated = service.rebuild();
		assertThat(repeated.counts()).isEqualTo(result.counts());
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1);

		try (AdminClient admin = AdminClient.create(Map.of(
				AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers()))) {
			Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> offsets = admin
					.listConsumerGroupOffsets("ranking-consumer-group").partitionsToOffsetAndMetadata().get(10, TimeUnit.SECONDS);
			assertThat(offsets).containsAllEntriesOf(result.endOffsets());
		}
	}

	@Test
	@Order(1)
	void mismatchFailsWithoutChangingLiveKeysOrNormalOffsetsAndCleansTemporaryKeys() throws Exception {
		publish(1L, LocalDateTime.of(2026, 7, 1, 10, 0));
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		redis.opsForZSet().add("popular:menus:2026-07-12", "77", 7);

		assertThatThrownBy(service::rebuild)
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("DB 집계");

		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "77")).isEqualTo(7);
		assertThat(redis.keys("rebuild:popular:menus:*")).isEmpty();
	}

	@Test
	@Order(2)
	void distributedLockRejectsConcurrentRunner() {
		redis.opsForValue().set(RankingRebuildService.LOCK_KEY, "other");

		assertThatThrownBy(service::rebuild)
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("이미 실행 중");
	}

	@Test
	@Order(3)
	void activeNormalConsumerMemberBlocksRebuild() throws Exception {
		publish(1L, LocalDateTime.of(2026, 7, 1, 10, 0));
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "ranking-consumer-group");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
			consumer.subscribe(java.util.List.of("order.completed"));
			long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
			while (consumer.assignment().isEmpty() && System.nanoTime() < deadline) {
				consumer.poll(Duration.ofMillis(200));
			}
			assertThat(consumer.assignment()).isNotEmpty();
			assertThatThrownBy(service::rebuild)
					.isInstanceOf(RankingRebuildException.class)
					.hasMessageContaining("활성 consumer");
		}
	}

	private void insertPaidOrder(Long menuId, LocalDateTime orderedAt) {
		jdbc.update("insert into user_point(user_id, balance) values (?,?) on duplicate key update balance=values(balance)",
				6101L, 10000);
		jdbc.update("insert into orders(user_id, menu_id, paid_amount, status, ordered_at) values (?,?,?,?,?)",
				6101L, menuId, 4500, "PAID", orderedAt);
	}

	private void publish(Long menuId, LocalDateTime orderedAt) throws Exception {
		OrderCompletedEvent event = new OrderCompletedEvent(UUID.randomUUID(), 1L, 6101L, menuId, 4500, orderedAt);
		kafkaTemplate.send("order.completed", "6101", event).get(10, TimeUnit.SECONDS);
		kafkaTemplate.flush();
	}
}
