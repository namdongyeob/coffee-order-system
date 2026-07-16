// maintenance mode에서 Kafka replay를 검증한 뒤 Redis 랭킹을 원자 교체합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.AlterConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "ranking.rebuild.maintenance", havingValue = "true")
public class RankingRebuildService {
	private static final Logger log = LoggerFactory.getLogger(RankingRebuildService.class);

	private static final String TOPIC = "order.completed";
	private static final String NORMAL_GROUP = "ranking-consumer-group";
	private static final String REBUILD_GROUP = "ranking-rebuild-group";
	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final Duration POLL_TIMEOUT = Duration.ofSeconds(30);
	private static final String TEMP_PREFIX = "rebuild:popular:menus:";
	private static final String LIVE_PREFIX = "popular:menus:";
	private static final String SWAP_MARKER_PREFIX = "ranking:rebuild:swap:";
	private static final String ORIGINAL_EXISTS_PREFIX = "ranking:rebuild:original-exists:";

	private final StringRedisTemplate redis;
	private final JdbcTemplate jdbc;
	private final RankingRebuildLock lock;
	private final RankingRebuildOffsetManager offsetManager;
	private final RankingRebuildLedger ledger;
	private final ObjectMapper objectMapper;
	private final String bootstrapServers;
	private final boolean maintenance;
	private final Instant configuredSnapshot;

	public RankingRebuildService(
			StringRedisTemplate redis,
			JdbcTemplate jdbc,
			RankingRebuildLock lock,
			RankingRebuildOffsetManager offsetManager,
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
			@Value("${ranking.rebuild.maintenance:false}") boolean maintenance,
			@Value("${ranking.rebuild.snapshot:}") String snapshot) {
		this(redis, jdbc, lock, offsetManager, new RankingRebuildLedger(jdbc), bootstrapServers, maintenance, snapshot);
	}

	@Autowired
	RankingRebuildService(
			StringRedisTemplate redis,
			JdbcTemplate jdbc,
			RankingRebuildLock lock,
			RankingRebuildOffsetManager offsetManager,
			RankingRebuildLedger ledger,
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
			@Value("${ranking.rebuild.maintenance:false}") boolean maintenance,
			@Value("${ranking.rebuild.snapshot:}") String snapshot) {
		this.redis = redis;
		this.jdbc = jdbc;
		this.lock = lock;
		this.offsetManager = offsetManager;
		this.ledger = ledger;
		this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		this.bootstrapServers = bootstrapServers;
		this.maintenance = maintenance;
		this.configuredSnapshot = snapshot.isBlank() ? null : Instant.parse(snapshot);
	}

	public RankingRebuildResult rebuild() {
		if (!maintenance) {
			throw new RankingRebuildException("maintenance mode에서만 rebuild를 실행할 수 있습니다");
		}
		String token = UUID.randomUUID().toString();
		if (!lock.acquire(token)) {
			throw new RankingRebuildException("다른 rebuild가 이미 실행 중입니다");
		}

		Instant snapshot = configuredSnapshot == null ? Instant.now() : configuredSnapshot;
		LocalDateTime upper = LocalDateTime.ofInstant(snapshot, SEOUL);
		LocalDateTime lower = upper.minusDays(7);
		String namespace = snapshot.toEpochMilli() + ":" + UUID.randomUUID();
		Set<String> dates = datesIntersecting(lower, upper);
		List<String> tempKeys = dates.stream().map(date -> tempKey(namespace, date)).toList();
		boolean keepLock = false;

		try (AdminClient admin = adminClient()) {
			assertNormalConsumerStopped(admin);
			try {
				if (recoverIncomplete(admin, token)) {
					return new RankingRebuildResult(Map.of(), Map.of(), 0, 0, 0);
				}
			} catch (RunExecutionException recoveryFailure) {
				keepLock = recoveryFailure.retainLock();
				throw recoveryFailure;
			} catch (RuntimeException recoveryFailure) {
				keepLock = true;
				throw recoveryFailure;
			}
			Map<TopicPartition, Long> capturedEnds = captureEndOffsets(admin);
			RankingRebuildOffsetManager.OffsetSnapshot previousOffsets =
					offsetManager.capture(admin, capturedEnds.keySet());
			ReplayResult replay = replay(capturedEnds, lower, upper, namespace);
			Map<RankingRebuildCount, Long> database = databaseCounts(lower, upper);
			if (!database.equals(replay.counts())) {
				throw new RankingRebuildException("Kafka replay와 DB 집계가 일치하지 않습니다");
			}
			requireLease(token);
			UUID runId = UUID.randomUUID();
			ledger.prepare(runId, replay.events().values(), namespace, dates, capturedEnds, previousOffsets,
					() -> requireLease(token));
			requireLease(token);
			try {
				atomicSwap(namespace, dates, runId);
			} catch (RuntimeException swapFailure) {
				// Redis 응답 timeout은 Lua 미실행과 실행 완료를 구분할 수 없으므로 plan을 삭제하지 않습니다.
				keepLock = true;
				throw swapFailure;
			}
			try {
				ledger.markSwapped(runId);
			} catch (RuntimeException markFailure) {
				keepLock = true;
				throw markFailure;
			}
			RankingRebuildLedger.RunPlan run = ledger.findIncomplete().orElseThrow(
					() -> new RankingRebuildException("swap된 rebuild run을 다시 조회하지 못했습니다"));
			try {
				completeSwappedRun(admin, run, token);
			} catch (RunExecutionException runFailure) {
				keepLock = runFailure.retainLock();
				throw runFailure;
			}
			Map<TopicPartition, OffsetAndMetadata> offsets = run.targetOffsets();
			return new RankingRebuildResult(
					Map.copyOf(replay.counts()), Map.copyOf(offsets), replay.inputRecordCount(),
					replay.uniqueEventCount(), replay.conflictCount());
		} catch (RankingRebuildException exception) {
			delete(tempKeys);
			throw exception;
		} catch (Exception exception) {
			delete(tempKeys);
			throw new RankingRebuildException("rebuild를 안전하게 완료하지 못했습니다", exception);
		} finally {
			if (!keepLock) {
				lock.release(token);
			}
		}
	}

	private boolean recoverIncomplete(AdminClient admin, String token) {
		RankingRebuildLedger.RunPlan run = ledger.findIncomplete().orElse(null);
		if (run == null) {
			return false;
		}
		if (RankingRebuildLedger.RECOVERY_REQUIRED.equals(run.state())) {
			throw new RankingRebuildException("RECOVERY_REQUIRED rebuild run은 운영자 확인 전 자동 진행할 수 없습니다");
		}
		if (RankingRebuildLedger.PREPARED.equals(run.state())) {
			requireRecoveryArtifacts(run);
			ledger.markSwapped(run.runId());
			run = ledger.findIncomplete().orElseThrow(
					() -> new RankingRebuildException("swap marker가 있는 rebuild run을 다시 조회하지 못했습니다"));
		}
		completeSwappedRun(admin, run, token);
		return true;
	}

	private void completeSwappedRun(AdminClient admin, RankingRebuildLedger.RunPlan run, String token) {
		if (RankingRebuildLedger.SWAPPED_PENDING_OFFSET.equals(run.state())
				|| RankingRebuildLedger.OFFSET_APPLIED_PENDING_LEDGER.equals(run.state())) {
			requireRecoveryArtifacts(run);
		}
		if (RankingRebuildLedger.SWAPPED_PENDING_OFFSET.equals(run.state())) {
			requireLease(token);
			try {
				offsetManager.move(admin, run.targetOffsets());
				offsetManager.verify(admin, run.targetOffsets());
			} catch (Exception offsetFailure) {
				CompensationResult compensation = compensateOffsetFailure(admin, run, offsetFailure);
				if (compensation.complete()) {
					try {
						ledger.cancel(run.runId());
					} catch (RuntimeException cancelFailure) {
						compensation.exception().addSuppressed(cancelFailure);
						try {
							ledger.markRecoveryRequired(run.runId());
						} catch (RuntimeException stateFailure) {
							compensation.exception().addSuppressed(stateFailure);
						}
						throw new RunExecutionException(compensation.exception().getMessage(),
								compensation.exception(), true);
					}
					try {
						cleanupRecoveryArtifacts(run);
					} catch (RuntimeException cleanupFailure) {
						compensation.exception().addSuppressed(cleanupFailure);
						throw new RunExecutionException("보상된 rebuild recovery artifact를 정리하지 못했습니다",
								compensation.exception(), false);
					}
					throw new RunExecutionException(compensation.exception().getMessage(),
							compensation.exception(), false);
				}
				try {
					ledger.markRecoveryRequired(run.runId());
				} catch (RuntimeException stateFailure) {
					compensation.exception().addSuppressed(stateFailure);
				}
				throw new RunExecutionException(compensation.exception().getMessage(),
						compensation.exception(), true);
			}
			try {
				ledger.markOffsetsApplied(run.runId());
			} catch (RuntimeException stateFailure) {
				throw new RunExecutionException("offset 적용 결과를 durable 상태로 기록하지 못했습니다",
						stateFailure, true);
			}
			run = ledger.findIncomplete().orElseThrow(
					() -> new RankingRebuildException("offset 적용 rebuild run을 다시 조회하지 못했습니다"));
		}
		else if (RankingRebuildLedger.OFFSET_APPLIED_PENDING_LEDGER.equals(run.state())) {
			requireLease(token);
			try {
				offsetManager.verify(admin, run.targetOffsets());
			} catch (Exception verifyFailure) {
				try {
					ledger.markRecoveryRequired(run.runId());
				} catch (RuntimeException stateFailure) {
					verifyFailure.addSuppressed(stateFailure);
				}
				throw new RunExecutionException("offset 적용 상태를 확인하지 못했습니다", verifyFailure, true);
			}
		}
		if (!RankingRebuildLedger.OFFSET_APPLIED_PENDING_LEDGER.equals(run.state())) {
			throw new RunExecutionException("복구할 수 없는 rebuild run 상태입니다: " + run.state(), null, true);
		}
		try {
			ledger.backfillAndComplete(run.runId(), () -> requireLease(token));
		} catch (RuntimeException ledgerFailure) {
			throw new RunExecutionException(
					"rebuild ledger backfill을 완료하지 못했습니다: " + ledgerFailure.getMessage(),
					ledgerFailure, true);
		}
		try {
			cleanupRecoveryArtifacts(run);
		} catch (RuntimeException cleanupFailure) {
			throw new RunExecutionException("완료된 rebuild recovery artifact를 정리하지 못했습니다",
					cleanupFailure, false);
		}
	}

	private void requireRecoveryArtifacts(RankingRebuildLedger.RunPlan run) {
		try {
			if (recoveryArtifactsIntact(run)) {
				return;
			}
			sealRecoveryRequired(run, null);
		} catch (RunExecutionException exception) {
			throw exception;
		} catch (RuntimeException validationFailure) {
			sealRecoveryRequired(run, validationFailure);
		}
	}

	private void sealRecoveryRequired(RankingRebuildLedger.RunPlan run, RuntimeException cause) {
		RankingRebuildException failure = new RankingRebuildException(
				"rebuild recovery artifact를 완전하게 확인할 수 없어 RECOVERY_REQUIRED로 봉인했습니다", cause);
		try {
			ledger.markRecoveryRequired(run.runId());
		} catch (RuntimeException stateFailure) {
			failure.addSuppressed(stateFailure);
		}
		throw new RunExecutionException(failure.getMessage(), failure, true);
	}

	private void requireLease(String token) {
		if (!lock.renew(token)) {
			throw new RankingRebuildException("rebuild lock 소유권을 잃어 위험 변경 전에 중단했습니다");
		}
	}

	private CompensationResult compensateOffsetFailure(
			AdminClient admin,
			RankingRebuildLedger.RunPlan run,
			Exception offsetFailure) {
		Exception offsetRestoreFailure = null;
		Exception redisRestoreFailure = null;
		try {
			offsetManager.restoreAndVerify(admin, run.previousOffsets());
		} catch (Exception exception) {
			offsetRestoreFailure = exception;
		}
		try {
			rollbackSwap(run.namespace(), run.dates(), run.runId());
		} catch (Exception exception) {
			redisRestoreFailure = exception;
		}
		if (offsetRestoreFailure != null || redisRestoreFailure != null) {
			RankingRebuildException incomplete = new RankingRebuildException(
					"offset 이동 실패 뒤 완전한 복원을 확인할 수 없습니다; 운영자 확인이 필요합니다", offsetFailure);
			if (offsetRestoreFailure != null) {
				incomplete.addSuppressed(offsetRestoreFailure);
			}
			if (redisRestoreFailure != null) {
				incomplete.addSuppressed(redisRestoreFailure);
			}
			return new CompensationResult(incomplete, false);
		}
		return new CompensationResult(
				new RankingRebuildException("offset 이동 실패 뒤 normal offset과 live Redis를 복원했습니다", offsetFailure),
				true);
	}

	private AdminClient adminClient() {
		return AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
	}

	private void assertNormalConsumerStopped(AdminClient admin) throws Exception {
		try {
			ConsumerGroupDescription description = admin.describeConsumerGroups(List.of(NORMAL_GROUP))
					.describedGroups().get(NORMAL_GROUP).get(10, TimeUnit.SECONDS);
			if (!description.members().isEmpty()) {
				throw new RankingRebuildException("ranking-consumer-group 활성 consumer가 있어 rebuild를 시작할 수 없습니다");
			}
		} catch (ExecutionException exception) {
			if (!(exception.getCause() instanceof GroupIdNotFoundException)) {
				throw exception;
			}
		}
	}

	private Map<TopicPartition, Long> captureEndOffsets(AdminClient admin) throws Exception {
		Collection<TopicPartition> partitions = admin.describeTopics(List.of(TOPIC)).allTopicNames()
				.get(10, TimeUnit.SECONDS).get(TOPIC).partitions().stream()
				.map(info -> new TopicPartition(TOPIC, info.partition())).toList();
		Map<TopicPartition, OffsetSpec> latest = partitions.stream()
				.collect(Collectors.toMap(partition -> partition, partition -> OffsetSpec.latest()));
		return admin.listOffsets(latest).all().get(10, TimeUnit.SECONDS).entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().offset()));
	}

	private ReplayResult replay(
			Map<TopicPartition, Long> ends,
			LocalDateTime lower,
			LocalDateTime upper,
			String namespace) throws Exception {
		Properties properties = new Properties();
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, REBUILD_GROUP);
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		Map<RankingRebuildCount, Long> counts = new HashMap<>();
		Map<UUID, RankingRebuildEvent> eventPayloads = new HashMap<>();
		long inputRecordCount = 0;
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
			consumer.assign(ends.keySet());
			consumer.seekToBeginning(ends.keySet());
			long deadline = System.nanoTime() + POLL_TIMEOUT.toNanos();
			while (!reachedEnds(consumer, ends)) {
				if (System.nanoTime() > deadline) {
					throw new RankingRebuildException("캡처한 Kafka end offset까지 도달하지 못했습니다");
				}
				for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(250))) {
					if (record.offset() >= ends.get(new TopicPartition(record.topic(), record.partition()))) {
						continue;
					}
					OrderCompletedEvent event = objectMapper.readValue(record.value(), OrderCompletedEvent.class);
					if (!event.orderedAt().isBefore(lower) && event.orderedAt().isBefore(upper)) {
						inputRecordCount++;
						RankingRebuildEvent payload = RankingRebuildEvent.from(event);
						RankingRebuildEvent previous = eventPayloads.putIfAbsent(event.eventId(), payload);
						if (previous != null) {
							if (!previous.equals(payload)) {
								log.error("ranking_rebuild_event_id_conflict eventId={} inputRecords={} uniqueEvents={} conflicts=1",
										event.eventId(), inputRecordCount, eventPayloads.size());
								throw new RankingRebuildException("rebuild eventId 충돌이 감지되었습니다");
							}
							continue;
						}
						RankingRebuildCount key = new RankingRebuildCount(event.orderedAt().toLocalDate().toString(), event.menuId());
						counts.merge(key, 1L, Long::sum);
						redis.opsForZSet().incrementScore(tempKey(namespace, key.date()), key.menuId().toString(), 1);
					}
				}
			}
		}
		return new ReplayResult(counts, inputRecordCount, eventPayloads.size(), 0, Map.copyOf(eventPayloads));
	}

	private record ReplayResult(
			Map<RankingRebuildCount, Long> counts,
			long inputRecordCount,
			long uniqueEventCount,
			long conflictCount,
			Map<UUID, RankingRebuildEvent> events) {
	}

	private boolean reachedEnds(KafkaConsumer<String, String> consumer, Map<TopicPartition, Long> ends) {
		return ends.entrySet().stream().allMatch(entry -> consumer.position(entry.getKey()) >= entry.getValue());
	}

	private Map<RankingRebuildCount, Long> databaseCounts(LocalDateTime lower, LocalDateTime upper) {
		Map<RankingRebuildCount, Long> counts = new HashMap<>();
		jdbc.query("select date(ordered_at) day, menu_id, count(*) total from orders "
					+ "where status = 'PAID' and ordered_at >= ? and ordered_at < ? "
					+ "group by date(ordered_at), menu_id",
				(RowCallbackHandler) resultSet -> counts.put(new RankingRebuildCount(
						resultSet.getDate("day").toLocalDate().toString(), resultSet.getLong("menu_id")),
						resultSet.getLong("total")), lower, upper);
		return counts;
	}

	private void atomicSwap(String namespace, Set<String> dates, UUID runId) {
		List<String> sortedDates = dates.stream().sorted().toList();
		List<String> keys = new ArrayList<>();
		keys.add(swapMarkerKey(runId));
		sortedDates.forEach(date -> {
			keys.add(tempKey(namespace, date));
			keys.add(LIVE_PREFIX + date);
			keys.add(backupKey(namespace, date));
			keys.add(originalExistsKey(runId, date));
		});
		String script = "for i=2,#KEYS,4 do "
				+ "redis.call('DEL',KEYS[i+2]); redis.call('DEL',KEYS[i+3]); "
				+ "local originalExists=redis.call('EXISTS',KEYS[i+1]); "
				+ "redis.call('SET',KEYS[i+3],tostring(originalExists)); "
				+ "if originalExists==1 then redis.call('RENAME',KEYS[i+1],KEYS[i+2]); redis.call('PERSIST',KEYS[i+2]) end; "
				+ "if redis.call('EXISTS',KEYS[i])==1 then redis.call('RENAME',KEYS[i],KEYS[i+1]) end end; "
				+ "redis.call('SET',KEYS[1],'SWAPPED'); return 1";
		redis.execute(new DefaultRedisScript<>(script, Long.class), keys);
	}

	private void rollbackSwap(String namespace, Set<String> dates, UUID runId) {
		List<String> keys = recoveryArtifactKeys(namespace, dates, runId, true);
		String script = recoveryArtifactValidationScript()
				+ "for i=2,#KEYS,3 do redis.call('DEL',KEYS[i]); "
				+ "if redis.call('GET',KEYS[i+2])=='1' then redis.call('COPY',KEYS[i+1],KEYS[i],'REPLACE') end end; "
				+ "return 1";
		Long restored = redis.execute(new DefaultRedisScript<>(script, Long.class), keys);
		if (!Long.valueOf(1L).equals(restored)) {
			throw new RankingRebuildException("rebuild recovery artifact 소실로 live Redis를 복원하지 않았습니다");
		}
	}

	private boolean recoveryArtifactsIntact(RankingRebuildLedger.RunPlan run) {
		List<String> keys = recoveryArtifactKeys(run.namespace(), run.dates(), run.runId(), true);
		Long valid = redis.execute(new DefaultRedisScript<>(recoveryArtifactValidationScript() + "return 1", Long.class),
				keys);
		return Long.valueOf(1L).equals(valid);
	}

	private String recoveryArtifactValidationScript() {
		return "if redis.call('GET',KEYS[1])~='SWAPPED' then return 0 end; "
				+ "for i=2,#KEYS,3 do local originalExists=redis.call('GET',KEYS[i+2]); "
				+ "if originalExists~='0' and originalExists~='1' then return 0 end; "
				+ "local backupExists=redis.call('EXISTS',KEYS[i+1]); "
				+ "if originalExists=='1' and backupExists~=1 then return 0 end; "
				+ "if originalExists=='0' and backupExists~=0 then return 0 end end; ";
	}

	private List<String> recoveryArtifactKeys(
			String namespace, Set<String> dates, UUID runId, boolean includeLiveKeys) {
		List<String> keys = new ArrayList<>();
		keys.add(swapMarkerKey(runId));
		dates.stream().sorted().forEach(date -> {
			if (includeLiveKeys) {
				keys.add(LIVE_PREFIX + date);
			}
			keys.add(backupKey(namespace, date));
			keys.add(originalExistsKey(runId, date));
		});
		return keys;
	}

	private void cleanupRecoveryArtifacts(RankingRebuildLedger.RunPlan run) {
		delete(recoveryArtifactKeys(run.namespace(), run.dates(), run.runId(), false));
	}

	private String swapMarkerKey(UUID runId) {
		return SWAP_MARKER_PREFIX + runId;
	}

	private String originalExistsKey(UUID runId, String date) {
		return ORIGINAL_EXISTS_PREFIX + runId + ":" + date;
	}

	static Set<String> datesIntersecting(LocalDateTime lower, LocalDateTime upper) {
		Set<String> dates = new java.util.LinkedHashSet<>();
		LocalDate date = lower.toLocalDate();
		LocalDate lastDate = upper.toLocalTime().equals(LocalTime.MIDNIGHT)
				? upper.toLocalDate().minusDays(1)
				: upper.toLocalDate();
		while (!date.isAfter(lastDate)) {
			dates.add(date.toString());
			date = date.plusDays(1);
		}
		return dates;
	}

	private String tempKey(String namespace, String date) {
		return TEMP_PREFIX + namespace + ":" + date;
	}

	private String backupKey(String namespace, String date) {
		return "rebuild:backup:popular:menus:" + namespace + ":" + date;
	}

	private void delete(List<String> keys) {
		if (!keys.isEmpty()) {
			redis.delete(keys);
		}
	}

	private record CompensationResult(RankingRebuildException exception, boolean complete) {
	}

	private static final class RunExecutionException extends RankingRebuildException {
		private final boolean retainLock;

		private RunExecutionException(String message, Throwable cause, boolean retainLock) {
			super(message, cause);
			this.retainLock = retainLock;
		}

		private boolean retainLock() {
			return retainLock;
		}
	}

}
