// capture 뒤 합류한 normal consumer와 rebuild의 Kafka·Redis·DB fencing을 검증합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

import com.example.coffeeordersystem.SharedTestcontainers;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.ranking.consumer.RankingEventFingerprint;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(properties = {
		"ranking.consumer.enabled=true",
		"ranking.rebuild.maintenance=true",
		"spring.kafka.consumer.properties.max.poll.interval.ms=3000"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(OutputCaptureExtension.class)
class RankingRebuildLateJoinIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final Instant SNAPSHOT = LocalDateTime.of(2026, 7, 13, 12, 0).atZone(SEOUL).toInstant();
	private static final String NORMAL_GROUP = "ranking-consumer-group";

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

	@Autowired KafkaTemplate<String, OrderCompletedEvent> kafkaTemplate;
	@Autowired KafkaListenerEndpointRegistry listenerRegistry;
	@Autowired StringRedisTemplate redis;
	@Autowired JdbcTemplate jdbc;
	@Autowired RankingRebuildOffsetManager offsetManager;
	@Autowired RankingRebuildService rebuildService;
	@MockitoSpyBean RankingRebuildLock recoveryLock;

	@BeforeEach
	void setUp() throws Exception {
		stopNormalConsumer();
		SharedTestcontainers.clearKafkaTopics();
		redis.getConnectionFactory().getConnection().serverCommands().flushAll();
		jdbc.update("delete from processed_event");
		jdbc.update("delete from ranking_rebuild_run_event");
		jdbc.update("delete from ranking_rebuild_run");
		jdbc.update("delete from ranking_event_ledger");
		jdbc.update("delete from orders");
		await(Duration.ofSeconds(10), () -> normalMemberCount() == 0);
	}

	@AfterEach
	void stopListener() {
		listenerRegistry.getListenerContainers().forEach(container -> container.stop());
	}

	@Test
	void captureThenOffsetEAttemptAbortsSwapAndEventuallyCommitsExactlyOnce(CapturedOutput output) throws Exception {
		OrderCompletedEvent captured = event(UUID.randomUUID(), 1L, 1L);
		insertPaidOrder(captured);
		publish(captured);

		CountDownLatch beforeSwap = new CountDownLatch(1);
		CountDownLatch continueSwap = new CountDownLatch(1);
		AtomicInteger renewals = new AtomicInteger();
		RankingRebuildLock blockingLock = new RankingRebuildLock(redis) {
			@Override
			public boolean renew(String token) {
				if (renewals.incrementAndGet() == 4) {
					beforeSwap.countDown();
					try {
						if (!continueSwap.await(10, TimeUnit.SECONDS)) {
							throw new AssertionError("pre-swap latch timeout");
						}
					} catch (InterruptedException exception) {
						Thread.currentThread().interrupt();
						throw new AssertionError("pre-swap latch interrupted", exception);
					}
				}
				return super.renew(token);
			}
		};
		RankingRebuildService rebuild = new RankingRebuildService(
				redis, jdbc, blockingLock, offsetManager,
				SharedTestcontainers.kafka().getBootstrapServers(), true, SNAPSHOT.toString());
		CompletableFuture<Throwable> rebuildResult = CompletableFuture.supplyAsync(() -> {
			try {
				rebuild.rebuild();
				return null;
			} catch (Throwable failure) {
				return failure;
			}
		});

		assertThat(beforeSwap.await(20, TimeUnit.SECONDS)).isTrue();
		CountDownLatch fenceAttempted = observeFailedConsumerFence(1);
		Map<TopicPartition, Long> capturedEnds = topicEnds();
		startNormalConsumer();
		OrderCompletedEvent late = event(UUID.randomUUID(), 2L, 2L);
		insertPaidOrder(late);
		RecordMetadata lateMetadata = publish(late);
		TopicPartition latePartition = new TopicPartition(lateMetadata.topic(), lateMetadata.partition());
		assertThat(lateMetadata.offset()).isEqualTo(capturedEnds.get(latePartition));
		long dltEnd = dltEnd(latePartition.partition());

		try {
			await(Duration.ofSeconds(5), () -> normalMemberCount() > 0);
			assertThat(fenceAttempted.await(10, TimeUnit.SECONDS)).isTrue();
			String lockOwner = redis.opsForValue().get(RankingRebuildLock.KEY);
			assertThat(lockOwner).startsWith("owner=REBUILD,runId=");
			await(Duration.ofSeconds(5), () -> output.getOut().contains("lockOwner=" + lockOwner));
			assertThat(jdbc.queryForObject(
					"select count(*) from ranking_event_ledger where event_id = ?",
					Long.class, late.eventId().toString())).isZero();
			assertThat(redis.opsForValue().get("ranking:applied-event:" + late.eventId())).isNull();
			assertThat(score(late)).isNull();
			assertThat(committedOffset(latePartition)).isLessThanOrEqualTo(lateMetadata.offset());
			assertThat(dltEnd(latePartition.partition())).isEqualTo(dltEnd);
		} finally {
			continueSwap.countDown();
		}

		Throwable failure = rebuildResult.get(20, TimeUnit.SECONDS);
		assertThatThrownBy(() -> {
			if (failure != null) {
				throw failure;
			}
		}).isInstanceOf(RankingRebuildException.class)
				.hasMessageContaining("활성 consumer")
				.hasMessageContaining("swap");

		await(Duration.ofSeconds(20), () -> "COMMITTED".equals(ledgerState(late.eventId())));
		await(Duration.ofSeconds(20), () -> committedOffset(latePartition) > lateMetadata.offset());
		assertThat(score(late)).isEqualTo(1.0);
		assertThat(redis.opsForValue().get("ranking:applied-event:" + late.eventId()))
				.isEqualTo(RankingEventFingerprint.from(late));
		assertThat(jdbc.queryForMap(
				"select state, source from ranking_event_ledger where event_id = ?", late.eventId().toString()))
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "NORMAL_CONSUMER");
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run", Long.class)).isZero();
	}

	@Test
	void consumerPausesBeyondMaxPollAndRetriesOriginalOffsetAfterFenceRelease() throws Exception {
		String token = "owner=REBUILD,runId=long-running-rebuild";
		assertThat(recoveryLock.acquire(token)).isTrue();
		CountDownLatch fenceAttempts = observeFailedConsumerFence(5);
		startNormalConsumer();
		OrderCompletedEvent event = event(UUID.randomUUID(), 3L, 3L);
		RecordMetadata metadata = publish(event);
		TopicPartition partition = new TopicPartition(metadata.topic(), metadata.partition());
		long dltEnd = dltEnd(partition.partition());

		try {
			assertThat(fenceAttempts.await(10, TimeUnit.SECONDS)).isTrue();
			assertThat(normalMemberCount()).isPositive();
			assertThat(ledgerState(event.eventId())).isNull();
			assertThat(score(event)).isNull();
			assertThat(committedOffset(partition)).isLessThanOrEqualTo(metadata.offset());
			assertThat(dltEnd(partition.partition())).isEqualTo(dltEnd);
		} finally {
			recoveryLock.release(token);
		}

		await(Duration.ofSeconds(20), () -> "COMMITTED".equals(ledgerState(event.eventId())));
		await(Duration.ofSeconds(20), () -> committedOffset(partition) > metadata.offset());
		assertThat(score(event)).isEqualTo(1.0);
	}

	@Test
	void offsetEAttemptThenMemberLeavesAllowsSwapAndRestartAppliesOffsetOnce() throws Exception {
		OrderCompletedEvent captured = event(UUID.randomUUID(), 4L, 1L);
		insertPaidOrder(captured);
		publish(captured);

		CountDownLatch beforeSwap = new CountDownLatch(1);
		CountDownLatch continueSwap = new CountDownLatch(1);
		AtomicInteger renewals = new AtomicInteger();
		RankingRebuildLock blockingLock = new RankingRebuildLock(redis) {
			@Override
			public boolean renew(String token) {
				if (renewals.incrementAndGet() == 4) {
					beforeSwap.countDown();
					try {
						if (!continueSwap.await(10, TimeUnit.SECONDS)) {
							throw new AssertionError("pre-swap latch timeout");
						}
					} catch (InterruptedException exception) {
						Thread.currentThread().interrupt();
						throw new AssertionError("pre-swap latch interrupted", exception);
					}
				}
				return super.renew(token);
			}
		};
		RankingRebuildService rebuild = new RankingRebuildService(
				redis, jdbc, blockingLock, offsetManager,
				SharedTestcontainers.kafka().getBootstrapServers(), true, SNAPSHOT.toString());
		CompletableFuture<RankingRebuildResult> rebuildResult = CompletableFuture.supplyAsync(rebuild::rebuild);

		assertThat(beforeSwap.await(20, TimeUnit.SECONDS)).isTrue();
		CountDownLatch fenceAttempted = observeFailedConsumerFence(1);
		Map<TopicPartition, Long> capturedEnds = topicEnds();
		startNormalConsumer();
		OrderCompletedEvent late = event(UUID.randomUUID(), 5L, 2L);
		insertPaidOrder(late);
		RecordMetadata lateMetadata = publish(late);
		TopicPartition latePartition = new TopicPartition(lateMetadata.topic(), lateMetadata.partition());
		assertThat(lateMetadata.offset()).isEqualTo(capturedEnds.get(latePartition));
		long dltEnd = dltEnd(latePartition.partition());
		await(Duration.ofSeconds(5), () -> normalMemberCount() > 0);
		assertThat(fenceAttempted.await(10, TimeUnit.SECONDS)).isTrue();
		assertThat(ledgerState(late.eventId())).isNull();
		assertThat(committedOffset(latePartition)).isLessThanOrEqualTo(lateMetadata.offset());
		assertThat(dltEnd(latePartition.partition())).isEqualTo(dltEnd);

		stopNormalConsumer();
		await(Duration.ofSeconds(10), () -> normalMemberCount() == 0);
		continueSwap.countDown();
		RankingRebuildResult result = rebuildResult.get(20, TimeUnit.SECONDS);
		assertThat(result.endOffsets().get(latePartition).offset()).isEqualTo(lateMetadata.offset());
		assertThat(score(late)).isNull();

		startNormalConsumer();
		await(Duration.ofSeconds(20), () -> "COMMITTED".equals(ledgerState(late.eventId())));
		await(Duration.ofSeconds(20), () -> committedOffset(latePartition) > lateMetadata.offset());
		assertThat(score(late)).isEqualTo(1.0);
		assertThat(redis.opsForValue().get("ranking:applied-event:" + late.eventId()))
				.isEqualTo(RankingEventFingerprint.from(late));
		assertThat(jdbc.queryForMap(
				"select state, source from ranking_event_ledger where event_id = ?", late.eventId().toString()))
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "NORMAL_CONSUMER");
		assertThat(dltEnd(latePartition.partition())).isEqualTo(dltEnd);
	}

	@Test
	void consumerFirstFenceOwnerBlocksRebuildThenEventCommitsExactlyOnce(CapturedOutput output) throws Exception {
		startNormalConsumer();
		await(Duration.ofSeconds(5), () -> normalMemberCount() > 0);
		CountDownLatch consumerOwnsFence = new CountDownLatch(1);
		CountDownLatch allowConsumerMutation = new CountDownLatch(1);
		AtomicReference<String> consumerOwner = new AtomicReference<>();
		doAnswer(invocation -> {
			String token = invocation.getArgument(0);
			boolean acquired = (boolean) invocation.callRealMethod();
			if (acquired && token.startsWith("owner=CONSUMER,")) {
				consumerOwner.set(token);
				consumerOwnsFence.countDown();
				if (!allowConsumerMutation.await(10, TimeUnit.SECONDS)) {
					throw new AssertionError("consumer fence release timeout");
				}
			}
			return acquired;
		}).when(recoveryLock).acquire(anyString());

		OrderCompletedEvent event = event(UUID.randomUUID(), 6L, 3L);
		RecordMetadata metadata = publish(event);
		TopicPartition partition = new TopicPartition(metadata.topic(), metadata.partition());
		long dltEnd = dltEnd(partition.partition());
		assertThat(consumerOwnsFence.await(10, TimeUnit.SECONDS)).isTrue();
		String owner = consumerOwner.get();
		assertThat(redis.opsForValue().get(RankingRebuildLock.KEY)).isEqualTo(owner);

		try {
			assertThatThrownBy(rebuildService::rebuild)
					.isInstanceOf(RankingRebuildException.class)
					.hasMessageContaining("shared recovery lock")
					.hasMessageContaining(owner)
					.hasMessageNotContaining("다른 rebuild");
			assertThat(output.getOut())
					.contains("reason=SHARED_RECOVERY_LOCK_BUSY")
					.contains("lockOwner=" + owner);
			assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run", Long.class)).isZero();
			assertThat(ledgerState(event.eventId())).isNull();
			assertThat(redis.opsForValue().get("ranking:applied-event:" + event.eventId())).isNull();
			assertThat(score(event)).isNull();
			assertThat(committedOffset(partition)).isLessThanOrEqualTo(metadata.offset());
			assertThat(dltEnd(partition.partition())).isEqualTo(dltEnd);
		} finally {
			allowConsumerMutation.countDown();
		}

		await(Duration.ofSeconds(20), () -> "COMMITTED".equals(ledgerState(event.eventId())));
		await(Duration.ofSeconds(20), () -> committedOffset(partition) > metadata.offset());
		assertThat(score(event)).isEqualTo(1.0);
		assertThat(redis.opsForValue().get("ranking:applied-event:" + event.eventId()))
				.isEqualTo(RankingEventFingerprint.from(event));
		assertThat(jdbc.queryForMap(
				"select state, source from ranking_event_ledger where event_id = ?", event.eventId().toString()))
				.containsEntry("state", "COMMITTED")
				.containsEntry("source", "NORMAL_CONSUMER");
		assertThat(jdbc.queryForObject("select count(*) from ranking_rebuild_run", Long.class)).isZero();
		assertThat(dltEnd(partition.partition())).isEqualTo(dltEnd);
	}

	private OrderCompletedEvent event(UUID eventId, long orderId, long menuId) {
		return new OrderCompletedEvent(
				eventId, orderId, 6101L, menuId, 4_500,
				LocalDateTime.of(2026, 7, 12, 10, 0).plusSeconds(orderId));
	}

	private void insertPaidOrder(OrderCompletedEvent event) {
		jdbc.update("insert into user_point(user_id, balance) values (?,?) "
				+ "on duplicate key update balance=values(balance)", event.userId(), 10_000);
		jdbc.update("insert into orders(user_id, menu_id, paid_amount, status, ordered_at) values (?,?,?,?,?)",
				event.userId(), event.menuId(), event.paidAmount(), "PAID", event.orderedAt());
	}

	private RecordMetadata publish(OrderCompletedEvent event) throws Exception {
		RecordMetadata metadata = kafkaTemplate.send(
				OrderEventPublisher.ORDER_COMPLETED_TOPIC, event.userId().toString(), event)
				.get(10, TimeUnit.SECONDS).getRecordMetadata();
		kafkaTemplate.flush();
		return metadata;
	}

	private void startNormalConsumer() throws Exception {
		int partitionCount = topicEnds().size();
		listenerRegistry.getListenerContainers().forEach(container -> {
			container.start();
			ContainerTestUtils.waitForAssignment(container, partitionCount);
		});
	}

	private void stopNormalConsumer() {
		listenerRegistry.getListenerContainers().forEach(container -> container.stop());
	}

	private long normalMemberCount() {
		try (AdminClient admin = adminClient()) {
			return admin.describeConsumerGroups(List.of(NORMAL_GROUP)).describedGroups().get(NORMAL_GROUP)
					.get(10, TimeUnit.SECONDS).members().size();
		} catch (Exception exception) {
			return 0;
		}
	}

	private Map<TopicPartition, Long> topicEnds() throws Exception {
		try (AdminClient admin = adminClient()) {
			List<TopicPartition> partitions = admin.describeTopics(List.of(OrderEventPublisher.ORDER_COMPLETED_TOPIC))
					.allTopicNames().get(10, TimeUnit.SECONDS).get(OrderEventPublisher.ORDER_COMPLETED_TOPIC)
					.partitions().stream()
					.map(info -> new TopicPartition(OrderEventPublisher.ORDER_COMPLETED_TOPIC, info.partition()))
					.toList();
			Map<TopicPartition, org.apache.kafka.clients.admin.OffsetSpec> latest = partitions.stream()
					.collect(java.util.stream.Collectors.toMap(partition -> partition,
							partition -> org.apache.kafka.clients.admin.OffsetSpec.latest()));
			return admin.listOffsets(latest).all().get(10, TimeUnit.SECONDS).entrySet().stream()
					.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().offset()));
		}
	}

	private long committedOffset(TopicPartition partition) {
		try (AdminClient admin = adminClient()) {
			OffsetAndMetadata offset = admin.listConsumerGroupOffsets(NORMAL_GROUP)
					.partitionsToOffsetAndMetadata().get(10, TimeUnit.SECONDS).get(partition);
			return offset == null ? -1 : offset.offset();
		} catch (Exception exception) {
			return -1;
		}
	}

	private String ledgerState(UUID eventId) {
		return jdbc.query(
				"select state from ranking_event_ledger where event_id = ?",
				(resultSet, rowNum) -> resultSet.getString(1), eventId.toString())
				.stream().findFirst().orElse(null);
	}

	private Double score(OrderCompletedEvent event) {
		return redis.opsForZSet().score("popular:menus:" + event.orderedAt().toLocalDate(), event.menuId().toString());
	}

	private AdminClient adminClient() {
		return AdminClient.create(Map.of(
				AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, SharedTestcontainers.kafka().getBootstrapServers()));
	}

	private CountDownLatch observeFailedConsumerFence(int attempts) {
		CountDownLatch attempted = new CountDownLatch(attempts);
		doAnswer(invocation -> {
			String token = invocation.getArgument(0);
			boolean acquired = (boolean) invocation.callRealMethod();
			if (!acquired && token.startsWith("owner=CONSUMER,")) {
				attempted.countDown();
			}
			return acquired;
		}).when(recoveryLock).acquire(anyString());
		return attempted;
	}

	private long dltEnd(int partition) throws Exception {
		try (AdminClient admin = adminClient()) {
			TopicPartition dlt = new TopicPartition(OrderEventPublisher.ORDER_COMPLETED_TOPIC + ".DLT", partition);
			try {
				return admin.listOffsets(Map.of(dlt, org.apache.kafka.clients.admin.OffsetSpec.latest()))
						.all().get(10, TimeUnit.SECONDS).get(dlt).offset();
			} catch (java.util.concurrent.ExecutionException exception) {
				if (exception.getCause() instanceof org.apache.kafka.common.errors.UnknownTopicOrPartitionException) {
					return 0L;
				}
				throw exception;
			}
		}
	}

	private void await(Duration timeout, BooleanSupplier condition) throws Exception {
		long deadline = System.nanoTime() + timeout.toNanos();
		while (System.nanoTime() < deadline) {
			if (condition.getAsBoolean()) {
				return;
			}
			Thread.sleep(100);
		}
		assertThat(condition.getAsBoolean()).isTrue();
	}
}
