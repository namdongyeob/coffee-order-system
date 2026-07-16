// Kafka replay 결과를 MySQL 원천과 비교해 Redis에 원자 반영하는 복구 계약을 검증합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.DisabledTaskSchedulerConfiguration;
import com.example.coffeeordersystem.SharedTestcontainers;
import com.example.coffeeordersystem.TestcontainersConfiguration;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
		"ranking.consumer.enabled=false",
		"ranking.rebuild.maintenance=true"
})
@Import({DisabledTaskSchedulerConfiguration.class, TestcontainersConfiguration.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RankingRebuildServiceIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final Instant SNAPSHOT = LocalDateTime.of(2026, 7, 13, 12, 0).atZone(SEOUL).toInstant();

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

	@Autowired RankingRebuildService service;
	@Autowired KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;
	@Autowired StringRedisTemplate redis;
	@Autowired JdbcTemplate jdbc;
	@Autowired RankingRebuildLock rebuildLock;
	@Autowired RankingRebuildOffsetManager offsetManager;
	@Autowired ProducerFactory<String, OrderCompletedEvent> producerFactory;

	@BeforeAll
	static void createTwoPartitionTopic() throws Exception {
		SharedTestcontainers.start();
		try (AdminClient admin = AdminClient.create(Map.of(
				AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, SharedTestcontainers.kafka().getBootstrapServers()))) {
			try {
				admin.createTopics(List.of(new NewTopic("order.completed", 2, (short) 1))).all().get(10, TimeUnit.SECONDS);
			} catch (java.util.concurrent.ExecutionException exception) {
				if (!(exception.getCause() instanceof org.apache.kafka.common.errors.TopicExistsException)) {
					throw exception;
				}
				int partitions = admin.describeTopics(List.of("order.completed")).allTopicNames()
						.get(10, TimeUnit.SECONDS).get("order.completed").partitions().size();
				if (partitions < 2) {
					admin.createPartitions(Map.of("order.completed", NewPartitions.increaseTo(2)))
							.all().get(10, TimeUnit.SECONDS);
				}
			}
		}
	}

	@BeforeEach
	void clean() throws Exception {
		redis.getConnectionFactory().getConnection().serverCommands().flushAll();
		jdbc.update("delete from processed_event");
		jdbc.update("delete from ranking_rebuild_run_event");
		jdbc.update("delete from ranking_rebuild_run");
		jdbc.update("delete from ranking_event_ledger");
		jdbc.update("delete from orders");
		deleteBeforeCurrentEnds();
	}

	@Test
	@Order(6)
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
				AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, SharedTestcontainers.kafka().getBootstrapServers()))) {
			Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> offsets = admin
					.listConsumerGroupOffsets("ranking-consumer-group").partitionsToOffsetAndMetadata().get(10, TimeUnit.SECONDS);
			assertThat(offsets).containsAllEntriesOf(result.endOffsets());
		}
	}

	@Test
	@Order(1)
	void retainedRecentEventsRebuildWhenCurrentEarliestOffsetIsGreaterThanZero() throws Exception {
		publish(1L, LocalDateTime.of(2026, 7, 1, 10, 0));
		deleteBeforeCurrentEnds();
		publish(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 10, 0));

		RankingRebuildResult result = service.rebuild();

		assertThat(result.counts()).containsEntry(new RankingRebuildCount("2026-07-12", 1L), 1L);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1);
	}

	@Test
	@Order(2)
	void retentionLossOfRequiredRecentEventFailsAndPreservesLiveKeysAndNormalOffsets() throws Exception {
		deleteBeforeCurrentEnds();
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		redis.opsForZSet().add("popular:menus:2026-07-12", "77", 7);
		Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> before = normalOffsets();

		assertThatThrownBy(service::rebuild)
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("DB 집계");

		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "77")).isEqualTo(7);
		assertThat(redis.keys("rebuild:popular:menus:*")).isEmpty();
		assertThat(normalOffsets()).isEqualTo(before);
		assertThat(jdbc.queryForObject("select count(*) from ranking_event_ledger", Long.class)).isZero();
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run", Long.class)).isZero();
	}

	@Test
	@Order(3)
	void distributedLockRejectsConcurrentRunner() {
		redis.opsForValue().set(RankingRebuildLock.KEY, "other", Duration.ofMillis(100));
		assertThat(rebuildLock.renew("other")).isTrue();
		assertThat(redis.getExpire(RankingRebuildLock.KEY, TimeUnit.MINUTES)).isGreaterThan(20);
		rebuildLock.release("not-owner");
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isEqualTo("other");

		assertThatThrownBy(service::rebuild)
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("이미 실행 중");
	}

	@Test
	@Order(4)
	void lostLeaseStopsBeforeSwapAndOffsetMutation() throws Exception {
		publish(1L, LocalDateTime.of(2026, 7, 1, 10, 0));
		redis.opsForZSet().add("popular:menus:2026-07-12", "77", 7);
		RankingRebuildLock lostLock = new RankingRebuildLock(redis) {
			@Override boolean acquire(String token) { return true; }
			@Override boolean renew(String token) { return false; }
			@Override void release(String token) { }
		};
		RankingRebuildService lostLeaseService = service(lostLock, offsetManager);

		assertThatThrownBy(lostLeaseService::rebuild)
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("lock 소유권");
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "77")).isEqualTo(7);
	}

	@Test
	@Order(5)
	void activeNormalConsumerMemberBlocksRebuild() throws Exception {
		publish(1L, LocalDateTime.of(2026, 7, 1, 10, 0));
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				SharedTestcontainers.kafka().getBootstrapServers());
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

	@Test
	@Order(7)
	void deduplicatesMatchingEventIdsAndExposesReplayMetrics() throws Exception {
		UUID eventId = UUID.randomUUID();
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0);
		insertPaidOrder(1L, orderedAt);
		publish(eventId, 1L, orderedAt);
		publish(eventId, 1L, orderedAt);
		publish(2L, LocalDateTime.of(2026, 7, 12, 11, 0));
		insertPaidOrder(2L, LocalDateTime.of(2026, 7, 12, 11, 0));

		RankingRebuildResult result = service.rebuild();

		assertThat(result.counts()).containsExactlyInAnyOrderEntriesOf(Map.of(
				new RankingRebuildCount("2026-07-12", 1L), 1L,
				new RankingRebuildCount("2026-07-12", 2L), 1L));
		assertThat(result.inputRecordCount()).isEqualTo(3);
		assertThat(result.uniqueEventCount()).isEqualTo(2);
		assertThat(result.conflictCount()).isZero();
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "2")).isEqualTo(1);
	}

	@Test
	@Order(11)
	void writesCommittedRebuildLedgerAfterSuccessfulSwap() throws Exception {
		UUID eventId = UUID.randomUUID();
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0);
		insertPaidOrder(1L, orderedAt);
		publish(eventId, 1L, orderedAt);

		service.rebuild();

		Map<String, Object> row = jdbc.queryForMap(
				"select event_id, event_type, payload_fingerprint, state, source, rebuild_run_id, committed_at "
						+ "from ranking_event_ledger where event_id = ?", eventId.toString());
		assertThat(row).containsEntry("event_id", eventId.toString())
				.containsEntry("event_type", "order.completed")
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "REBUILD")
				.containsKey("payload_fingerprint")
				.containsKey("rebuild_run_id")
				.containsKey("committed_at");
	}

	@Test
	@Order(12)
	void repeatedRebuildKeepsOneLedgerRowForTheSameEvent() throws Exception {
		UUID eventId = UUID.randomUUID();
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0, 0, 123_456_789);
		insertPaidOrder(1L, orderedAt);
		publish(eventId, 1L, orderedAt);

		service.rebuild();
		service.rebuild();

		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_event_ledger where event_id = ?", Long.class, eventId.toString()))
				.isEqualTo(1L);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1);
	}

	@Test
	@Order(13)
	void differentExistingLedgerFingerprintFailsBeforeSwap() throws Exception {
		UUID eventId = UUID.randomUUID();
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0);
		insertPaidOrder(1L, orderedAt);
		publish(eventId, 1L, orderedAt);
		RankingRebuildEvent conflicting = new RankingRebuildEvent(eventId, 1L, 6101L, 2L, 4500, orderedAt);
		jdbc.update("insert into ranking_event_ledger(event_id, event_type, payload_fingerprint, state, source, "
				+ "reserved_at, committed_at) values (?,?,?,?,?,?,?)",
				eventId.toString(), "order.completed", conflicting.payloadFingerprint(), "COMMITTED", "DLT_REPLAY",
				orderedAt, orderedAt);
		redis.opsForZSet().add("popular:menus:2026-07-12", "77", 7);

		assertThatThrownBy(service::rebuild)
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("EVENT_ID_PAYLOAD_CONFLICT");

		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "77")).isEqualTo(7);
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run", Long.class)).isZero();
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isNull();
	}

	@Test
	@Order(14)
	void retryAfterLedgerFailureCompletesTheSamePendingRunWithoutAnotherSwap() throws Exception {
		UUID eventId = UUID.randomUUID();
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0);
		insertPaidOrder(1L, orderedAt);
		publish(eventId, 1L, orderedAt);
		RankingRebuildLedger interruptedLedger = new RankingRebuildLedger(jdbc) {
			private boolean firstBackfill = true;

			@Override
			void backfillAndComplete(UUID runId, Runnable heartbeat) {
				if (firstBackfill) {
					firstBackfill = false;
					throw new RankingRebuildException("injected ledger backfill interruption");
				}
				super.backfillAndComplete(runId, heartbeat);
			}
		};

		assertThatThrownBy(() -> service(rebuildLock, offsetManager, interruptedLedger).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("injected ledger backfill interruption");
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1);
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run where state = 'OFFSET_APPLIED_PENDING_LEDGER'", Long.class))
				.isEqualTo(1L);
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isNotNull();

		redis.delete(RankingRebuildLock.KEY);
		service.rebuild();

		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run", Long.class)).isEqualTo(1L);
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run where state = 'COMPLETED'", Long.class)).isEqualTo(1L);
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_event_ledger where event_id = ? and state = 'COMMITTED'",
				Long.class, eventId.toString())).isEqualTo(1L);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1);
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isNull();
	}

	@Test
	@Order(15)
	void recoversSameRunWhenDatabaseMarkFailsImmediatelyAfterAtomicSwap() throws Exception {
		UUID eventId = UUID.randomUUID();
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0);
		insertPaidOrder(1L, orderedAt);
		publish(eventId, 1L, orderedAt);
		redis.opsForZSet().add("popular:menus:2026-07-12", "77", 7);
		RankingRebuildLedger interruptedLedger = new RankingRebuildLedger(jdbc) {
			@Override
			void markSwapped(UUID runId) {
				throw new RankingRebuildException("injected failure before DB swap mark");
			}
		};

		assertThatThrownBy(() -> service(rebuildLock, offsetManager, interruptedLedger).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("injected failure before DB swap mark");

		String runId = jdbc.queryForObject("select run_id from ranking_rebuild_run", String.class);
		assertThat(jdbc.queryForObject("select state from ranking_rebuild_run where run_id = ?", String.class, runId))
				.isEqualTo("PREPARED");
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run_event where run_id = ?", Long.class, runId)).isEqualTo(1L);
		assertThat(redis.opsForValue().get("ranking:rebuild:swap:" + runId)).isEqualTo("SWAPPED");
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1);
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isNotNull();

		redis.delete(RankingRebuildLock.KEY);
		RankingRebuildResult recovered = service.rebuild();

		assertThat(recovered.inputRecordCount()).isZero();
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run", Long.class)).isEqualTo(1L);
		assertThat(jdbc.queryForObject("select state from ranking_rebuild_run where run_id = ?", String.class, runId))
				.isEqualTo("COMPLETED");
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(1);
		assertThat(redis.opsForValue().get("ranking:rebuild:swap:" + runId)).isNull();
	}

	@Test
	@Order(8)
	void rejectsConflictingPayloadsForTheSameEventId() throws Exception {
		UUID eventId = UUID.randomUUID();
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0);
		publish(eventId, 1L, orderedAt);
		publish(eventId, 2L, orderedAt);

		assertThatThrownBy(service::rebuild)
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("eventId 충돌");
		assertThat(redis.keys("rebuild:popular:menus:*")).isEmpty();
	}

	@Test
	@Order(9)
	void partialOffsetMoveTimeoutRestoresAllOffsetsAndLiveRedis() throws Exception {
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		insertPaidOrder(2L, LocalDateTime.of(2026, 7, 6, 12, 0));
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 11, 0));
		publish(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		publish(2L, LocalDateTime.of(2026, 7, 6, 12, 0));
		publish(1L, LocalDateTime.of(2026, 7, 12, 11, 0));
		redis.opsForZSet().add("popular:menus:2026-07-12", "77", 7);
		Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> before = normalOffsets();
		RankingRebuildOffsetManager partialTimeout = new RankingRebuildOffsetManager() {
			@Override
			void move(AdminClient admin, Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> target)
					throws Exception {
				Map.Entry<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> first = target.entrySet().iterator().next();
				super.move(admin, Map.of(first.getKey(), first.getValue()));
				throw new TimeoutException("injected after partial broker update");
			}
		};

		assertThatThrownBy(() -> service(rebuildLock, partialTimeout).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("복원했습니다");
		assertThat(normalOffsets()).isEqualTo(before);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "77")).isEqualTo(7);
		assertThat(redis.keys("rebuild:*" )).isEmpty();
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run", Long.class)).isZero();

		RankingRebuildResult retried = service.rebuild();
		assertThat(retried.inputRecordCount()).isEqualTo(3);
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run where state = 'COMPLETED'", Long.class)).isEqualTo(1L);
	}

	@Test
	@Order(10)
	void compensationFailureReportsUncertainOffsetStateWithoutClaimingRollback() throws Exception {
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		insertPaidOrder(2L, LocalDateTime.of(2026, 7, 6, 12, 0));
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 11, 0));
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 11, 30));
		publish(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		publish(2L, LocalDateTime.of(2026, 7, 6, 12, 0));
		publish(1L, LocalDateTime.of(2026, 7, 12, 11, 0));
		publish(1L, LocalDateTime.of(2026, 7, 12, 11, 30));
		RankingRebuildOffsetManager failedCompensation = new RankingRebuildOffsetManager() {
			@Override
			void move(AdminClient admin, Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> target)
					throws Exception {
				super.move(admin, target);
				throw new TimeoutException("injected uncertain completion");
			}

			@Override
			void restoreAndVerify(AdminClient admin, OffsetSnapshot snapshot) {
				throw new RankingRebuildException("injected compensation failure");
			}
		};

		assertThatThrownBy(() -> service(rebuildLock, failedCompensation).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("완전한 복원을 확인할 수 없습니다");
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run where state = 'RECOVERY_REQUIRED'", Long.class)).isEqualTo(1L);
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run_event", Long.class)).isEqualTo(4L);
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isNotNull();
	}

	@Test
	@Order(16)
	void partialOffsetMoveCrashResumesCapturedEndsForTheSameRun() throws Exception {
		insertPaidOrder(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		insertPaidOrder(2L, LocalDateTime.of(2026, 7, 12, 11, 0));
		publish(1L, LocalDateTime.of(2026, 7, 12, 10, 0));
		publish(2L, LocalDateTime.of(2026, 7, 12, 11, 0));
		Map<TopicPartition, Long> capturedEnds = topicEnds();
		RankingRebuildOffsetManager crashAfterFirstPartition = new RankingRebuildOffsetManager() {
			@Override
			void move(AdminClient admin, Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> target)
					throws Exception {
				Map.Entry<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> first =
						target.entrySet().iterator().next();
				super.move(admin, Map.of(first.getKey(), first.getValue()));
				throw new AssertionError("injected process crash after one partition offset");
			}
		};

		assertThatThrownBy(() -> service(rebuildLock, crashAfterFirstPartition).rebuild())
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("injected process crash");
		String runId = jdbc.queryForObject("select run_id from ranking_rebuild_run", String.class);

		redis.delete(RankingRebuildLock.KEY);
		RankingRebuildResult recovered = service.rebuild();

		assertThat(recovered.inputRecordCount()).isZero();
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run", Long.class)).isEqualTo(1L);
		assertThat(jdbc.queryForObject("select state from ranking_rebuild_run where run_id = ?", String.class, runId))
				.isEqualTo("COMPLETED");
		Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> offsets = normalOffsets();
		capturedEnds.forEach((partition, end) -> assertThat(offsets.get(partition).offset()).isEqualTo(end));
	}

	@Test
	@Order(17)
	void failedLeaseRenewalBetweenLedgerBatchesKeepsRunPending() throws Exception {
		for (long sequence = 1; sequence <= 101; sequence++) {
			LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0).plusSeconds(sequence);
			insertPaidOrder(1L, orderedAt);
			publish(1L, orderedAt);
		}
		AtomicInteger renewCalls = new AtomicInteger();
		RankingRebuildLock shortLease = new RankingRebuildLock(redis) {
			@Override
			boolean acquire(String token) {
				return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(KEY, token, Duration.ofSeconds(5)));
			}

			@Override
			boolean renew(String token) {
				if (renewCalls.incrementAndGet() == 9) {
					return false;
				}
				Long renewed = redis.execute(new DefaultRedisScript<>(
						"if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('PEXPIRE',KEYS[1],5000) else return 0 end",
						Long.class), List.of(KEY), token);
				return Long.valueOf(1L).equals(renewed);
			}
		};

		assertThatThrownBy(() -> service(shortLease, offsetManager).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("lock 소유권");

		assertThat(renewCalls.get()).isGreaterThanOrEqualTo(9);
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run where state = 'OFFSET_APPLIED_PENDING_LEDGER'", Long.class))
				.isEqualTo(1L);
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run where state = 'COMPLETED'", Long.class)).isZero();
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isNotNull();
	}

	@Test
	@Order(18)
	void partialLedgerBackfillAndOffsetVerifyFailurePreserveTheSameRecoveryPlan() throws Exception {
		seedEvents(101);
		RankingRebuildLedger partialBackfill = new RankingRebuildLedger(jdbc) {
			private boolean first = true;

			@Override
			void backfillAndComplete(UUID runId, Runnable heartbeat) {
				if (!first) {
					super.backfillAndComplete(runId, heartbeat);
					return;
				}
				first = false;
				AtomicInteger batches = new AtomicInteger();
				super.backfillAndComplete(runId, () -> {
					if (batches.incrementAndGet() == 2) {
						throw new RankingRebuildException("injected after first ledger batch");
					}
					heartbeat.run();
				});
			}
		};

		assertThatThrownBy(() -> service(rebuildLock, offsetManager, partialBackfill).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("first ledger batch");
		assertThat(jdbc.queryForObject("select count(*) from ranking_event_ledger", Long.class)).isEqualTo(50L);
		String runId = jdbc.queryForObject("select run_id from ranking_rebuild_run", String.class);

		redis.delete(RankingRebuildLock.KEY);
		RankingRebuildOffsetManager verifyFailure = new RankingRebuildOffsetManager() {
			@Override
			void verify(AdminClient admin, Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> expected)
					throws Exception {
				throw new TimeoutException("injected offset verification outage");
			}
		};

		assertThatThrownBy(() -> service(rebuildLock, verifyFailure).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("offset")
				.hasMessageContaining("확인");

		assertThat(jdbc.queryForObject("select state from ranking_rebuild_run where run_id = ?", String.class, runId))
				.isEqualTo("RECOVERY_REQUIRED");
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run_event", Long.class)).isEqualTo(101L);
		assertThat(jdbc.queryForObject("select count(*) from ranking_event_ledger", Long.class)).isEqualTo(50L);
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "1")).isEqualTo(101);
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isNotNull();
	}

	@Test
	@Order(19)
	void cancelFailureAfterCompleteCompensationSealsTheRunAgainstAutomaticRecovery() throws Exception {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0);
		insertPaidOrder(1L, orderedAt);
		publish(1L, orderedAt);
		redis.opsForZSet().add("popular:menus:2026-07-12", "77", 7);
		Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> before = normalOffsets();
		RankingRebuildOffsetManager moveFailure = new RankingRebuildOffsetManager() {
			@Override
			void move(AdminClient admin, Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> target)
					throws Exception {
				throw new TimeoutException("injected before offset move");
			}
		};
		RankingRebuildLedger cancelFailure = new RankingRebuildLedger(jdbc) {
			@Override
			void cancel(UUID runId) {
				throw new RankingRebuildException("injected cancel failure");
			}
		};

		assertThatThrownBy(() -> service(rebuildLock, moveFailure, cancelFailure).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("복원했습니다");

		String runId = jdbc.queryForObject("select run_id from ranking_rebuild_run", String.class);
		assertThat(jdbc.queryForObject("select state from ranking_rebuild_run where run_id = ?", String.class, runId))
				.isEqualTo("RECOVERY_REQUIRED");
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run_event", Long.class)).isEqualTo(1L);
		assertThat(redis.opsForValue().get("ranking:rebuild:swap:" + runId)).isNull();
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "77")).isEqualTo(7);
		assertThat(normalOffsets()).isEqualTo(before);
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isNotNull();

		redis.delete(RankingRebuildLock.KEY);
		assertThatThrownBy(service::rebuild)
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("RECOVERY_REQUIRED");
		assertThat(jdbc.queryForObject("select state from ranking_rebuild_run where run_id = ?", String.class, runId))
				.isEqualTo("RECOVERY_REQUIRED");
		assertThat(jdbc.queryForObject("select count(*) from ranking_event_ledger", Long.class)).isZero();
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "77")).isEqualTo(7);
	}

	@Test
	@Order(20)
	void leaseLossDuringSecondPrepareBatchStopsBeforeSwap() throws Exception {
		seedEvents(51);
		redis.opsForZSet().add("popular:menus:2026-07-12", "77", 7);
		AtomicInteger renewCalls = new AtomicInteger();
		RankingRebuildLock changingOwner = ownershipChangingLock(3, renewCalls);

		assertThatThrownBy(() -> service(changingOwner, offsetManager).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("lock 소유권");

		assertThat(renewCalls.get()).isEqualTo(3);
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run where state = 'PREPARED'", Long.class)).isEqualTo(1L);
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run_event", Long.class)).isEqualTo(50L);
		assertThat(jdbc.queryForObject("select count(*) from ranking_event_ledger", Long.class)).isZero();
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "77")).isEqualTo(7);
		assertThat(redis.keys("ranking:rebuild:swap:*")).isEmpty();
	}

	@Test
	@Order(21)
	void ownershipChangeAfterPreparedPlanIsRecheckedImmediatelyBeforeSwap() throws Exception {
		seedEvents(51);
		redis.opsForZSet().add("popular:menus:2026-07-12", "77", 7);
		AtomicInteger renewCalls = new AtomicInteger();
		RankingRebuildLock changingOwner = ownershipChangingLock(5, renewCalls);

		assertThatThrownBy(() -> service(changingOwner, offsetManager).rebuild())
				.isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("lock 소유권");

		assertThat(renewCalls.get()).isEqualTo(5);
		assertThat(jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run where state = 'PREPARED'", Long.class)).isEqualTo(1L);
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run_event", Long.class)).isEqualTo(51L);
		assertThat(jdbc.queryForObject("select count(*) from ranking_event_ledger", Long.class)).isZero();
		assertThat(redis.opsForZSet().score("popular:menus:2026-07-12", "77")).isEqualTo(7);
		assertThat(redis.keys("ranking:rebuild:swap:*")).isEmpty();
	}

	private RankingRebuildService service(RankingRebuildLock lock, RankingRebuildOffsetManager manager) {
		return new RankingRebuildService(redis, jdbc, lock, manager,
				SharedTestcontainers.kafka().getBootstrapServers(), true, SNAPSHOT.toString());
	}

	private RankingRebuildService service(
			RankingRebuildLock lock,
			RankingRebuildOffsetManager manager,
			RankingRebuildLedger ledger) {
		return new RankingRebuildService(redis, jdbc, lock, manager, ledger,
				SharedTestcontainers.kafka().getBootstrapServers(), true, SNAPSHOT.toString());
	}

	private Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> normalOffsets() throws Exception {
		try (AdminClient admin = AdminClient.create(Map.of(
				AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, SharedTestcontainers.kafka().getBootstrapServers()))) {
			return admin.listConsumerGroupOffsets("ranking-consumer-group")
					.partitionsToOffsetAndMetadata().get(10, TimeUnit.SECONDS);
		}
	}

	private Map<TopicPartition, Long> topicEnds() throws Exception {
		try (AdminClient admin = AdminClient.create(Map.of(
				AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, SharedTestcontainers.kafka().getBootstrapServers()))) {
			List<TopicPartition> partitions = admin.describeTopics(List.of("order.completed")).allTopicNames()
					.get(10, TimeUnit.SECONDS).get("order.completed").partitions().stream()
					.map(info -> new TopicPartition("order.completed", info.partition())).toList();
			Map<TopicPartition, OffsetSpec> latest = partitions.stream()
					.collect(java.util.stream.Collectors.toMap(partition -> partition, partition -> OffsetSpec.latest()));
			return admin.listOffsets(latest).all().get(10, TimeUnit.SECONDS).entrySet().stream()
					.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().offset()));
		}
	}

	private void deleteBeforeCurrentEnds() throws Exception {
		try (AdminClient admin = AdminClient.create(Map.of(
				AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, SharedTestcontainers.kafka().getBootstrapServers()))) {
			List<TopicPartition> partitions = admin.describeTopics(List.of("order.completed")).allTopicNames()
					.get(10, TimeUnit.SECONDS).get("order.completed").partitions().stream()
					.map(info -> new TopicPartition("order.completed", info.partition())).toList();
			Map<TopicPartition, OffsetSpec> latest = partitions.stream()
					.collect(java.util.stream.Collectors.toMap(partition -> partition, partition -> OffsetSpec.latest()));
			Map<TopicPartition, org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo> ends =
					admin.listOffsets(latest).all().get(10, TimeUnit.SECONDS);
			Map<TopicPartition, RecordsToDelete> deletes = ends.entrySet().stream()
					.filter(entry -> entry.getValue().offset() > 0)
					.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
							entry -> RecordsToDelete.beforeOffset(entry.getValue().offset())));
			if (!deletes.isEmpty()) {
				admin.deleteRecords(deletes).all().get(10, TimeUnit.SECONDS);
			}
		}
	}

	private void insertPaidOrder(Long menuId, LocalDateTime orderedAt) {
		jdbc.update("insert into user_point(user_id, balance) values (?,?) on duplicate key update balance=values(balance)",
				6101L, 10000);
		jdbc.update("insert into orders(user_id, menu_id, paid_amount, status, ordered_at) values (?,?,?,?,?)",
				6101L, menuId, 4500, "PAID", orderedAt);
	}

	private void seedEvents(int count) throws Exception {
		for (int sequence = 1; sequence <= count; sequence++) {
			LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0).plusSeconds(sequence);
			insertPaidOrder(1L, orderedAt);
			publish(1L, orderedAt);
		}
	}

	private RankingRebuildLock ownershipChangingLock(int failAtRenew, AtomicInteger renewCalls) {
		return new RankingRebuildLock(redis) {
			@Override
			boolean renew(String token) {
				if (renewCalls.incrementAndGet() == failAtRenew) {
					redis.opsForValue().set(KEY, "other-owner", Duration.ofMinutes(5));
					return false;
				}
				return super.renew(token);
			}
		};
	}

	private void publish(Long menuId, LocalDateTime orderedAt) throws Exception {
		publish(UUID.randomUUID(), menuId, orderedAt);
	}

	private void publish(UUID eventId, Long menuId, LocalDateTime orderedAt) throws Exception {
		OrderCompletedEvent event = new OrderCompletedEvent(eventId, 1L, 6101L, menuId, 4500, orderedAt);
		kafkaTemplate.send("order.completed", "6101", event).get(10, TimeUnit.SECONDS);
		kafkaTemplate.flush();
	}
}
