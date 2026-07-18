package com.example.coffeeordersystem.ranking.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.coffeeordersystem.TestcontainersConfiguration;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"ranking.consumer.enabled=false",
		"ranking.ledger.cleanup.enabled=false",
		"ranking.ledger.cleanup.batch-size=2"
})
class RankingLedgerCleanupIntegrationTest {

	private static final Instant NOW = Instant.parse("2026-07-18T04:00:00Z");
	private static final Instant CUTOFF = Instant.parse("2026-06-18T04:00:00Z");

	@Autowired JdbcTemplate jdbc;
	@Autowired RankingLedgerCleanup configuredCleanup;

	@BeforeEach
	void setUp() {
		jdbc.update("delete from ranking_event_ledger");
		jdbc.update("delete from ranking_rebuild_run_offset");
		jdbc.update("delete from ranking_rebuild_run_event");
		jdbc.update("delete from ranking_rebuild_run");
	}

	@Test
	void deletesOnlyCommittedRowsStrictlyBeforeTheFixedCutoff() {
		String before = insertLedger("COMMITTED", CUTOFF.minusNanos(1_000), null);
		String exact = insertLedger("COMMITTED", CUTOFF, null);
		String after = insertLedger("COMMITTED", CUTOFF.plusNanos(1_000), null);
		String reserved = insertLedger("RESERVED", null, null);
		String redisApplied = insertLedger("REDIS_APPLIED", null, null);

		int deleted = cleanup(100).cleanupOneBatch();

		assertThat(deleted).isOne();
		assertThat(existingEventIds())
				.containsExactlyInAnyOrder(exact, after, reserved, redisApplied)
				.doesNotContain(before);
	}

	@Test
	void preservesEveryIncompleteRebuildStateAndDeletesCompletedRebuildRows() {
		String prepared = insertRebuildLedger("PREPARED");
		String swapped = insertRebuildLedger("SWAPPED_PENDING_OFFSET");
		String offsetsApplied = insertRebuildLedger("OFFSET_APPLIED_PENDING_LEDGER");
		String recoveryRequired = insertRebuildLedger("RECOVERY_REQUIRED");
		String completed = insertRebuildLedger("COMPLETED");

		int deleted = cleanup(100).cleanupOneBatch();

		assertThat(deleted).isOne();
		assertThat(existingEventIds())
				.containsExactlyInAnyOrder(prepared, swapped, offsetsApplied, recoveryRequired)
				.doesNotContain(completed);
	}

	@Test
	void eachInvocationDeletesAtMostOneBatchAndRerunsEndAtZero() {
		insertLedger("COMMITTED", CUTOFF.minus(Duration.ofDays(3)), null);
		insertLedger("COMMITTED", CUTOFF.minus(Duration.ofDays(2)), null);
		insertLedger("COMMITTED", CUTOFF.minus(Duration.ofDays(1)), null);
		RankingLedgerCleanup cleanup = cleanup(2);

		assertThat(cleanup.cleanupOneBatch()).isEqualTo(2);
		assertThat(existingEventIds()).hasSize(1);
		assertThat(cleanup.cleanupOneBatch()).isOne();
		assertThat(cleanup.cleanupOneBatch()).isZero();
		assertThat(existingEventIds()).isEmpty();
	}

	@Test
	void concurrentInvocationsKeepEachBatchBoundedWithoutDuplicateDeleteErrors() throws Exception {
		for (int index = 0; index < 6; index++) {
			insertLedger("COMMITTED", CUTOFF.minus(Duration.ofDays(index + 1L)), null);
		}
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<Integer> first = executor.submit(() -> {
				start.await();
				return configuredCleanup.cleanupOneBatch();
			});
			Future<Integer> second = executor.submit(() -> {
				start.await();
				return configuredCleanup.cleanupOneBatch();
			});
			start.countDown();

			int firstDeleted = first.get(30, TimeUnit.SECONDS);
			int secondDeleted = second.get(30, TimeUnit.SECONDS);
			assertThat(firstDeleted).isBetween(0, 2);
			assertThat(secondDeleted).isBetween(0, 2);
			assertThat(existingEventIds()).hasSize(6 - firstDeleted - secondDeleted);
		} finally {
			executor.shutdownNow();
			assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
		}
	}

	@Test
	void cleanupIndexHasStateCommittedAtOrderAndExplainUsesIt() {
		java.util.List<String> columns = jdbc.queryForList(
				"select column_name from information_schema.statistics "
						+ "where table_schema = database() and table_name = 'ranking_event_ledger' "
						+ "and index_name = 'idx_ranking_event_ledger_cleanup' order by seq_in_index",
				String.class);
		assertThat(columns).containsExactly("state", "committed_at");

		Map<String, Object> plan = jdbc.queryForList(
				"explain select ledger.event_id from ranking_event_ledger ledger "
						+ "force index (idx_ranking_event_ledger_cleanup) "
						+ "left join ranking_rebuild_run rebuild on rebuild.run_id = ledger.rebuild_run_id "
						+ "where ledger.state = 'COMMITTED' and ledger.committed_at is not null "
						+ "and ledger.committed_at < ? "
						+ "and (ledger.rebuild_run_id is null or rebuild.state = 'COMPLETED') "
						+ "order by ledger.committed_at, ledger.event_id limit 100",
				Timestamp.from(CUTOFF)).stream()
				.filter(row -> "ledger".equals(row.get("table")))
				.findFirst()
				.orElseThrow();
		assertThat(plan.get("key")).isEqualTo("idx_ranking_event_ledger_cleanup");
		assertThat(plan.get("type")).isEqualTo("range");
	}

	@Test
	void unknownOrLongerExternalRetentionFailsClosedBeforeDeletingRows() {
		insertLedger("COMMITTED", CUTOFF.minus(Duration.ofDays(1)), null);
		RankingLedgerRetentionProperties invalid = new RankingLedgerRetentionProperties(
				true,
				Duration.ofDays(30),
				Duration.ofDays(30),
				Duration.ofDays(31),
				Duration.ofDays(30),
				Duration.ofDays(30),
				100,
				Duration.ofHours(1));
		RankingLedgerRetentionPolicy policy = mock(RankingLedgerRetentionPolicy.class);
		when(policy.cutoff()).thenReturn(CUTOFF);
		RankingLedgerCleanup cleanup = new RankingLedgerCleanup(
				new RankingLedgerCleanupRepository(jdbc), policy, invalid);

		assertThatThrownBy(cleanup::cleanupOneBatch)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("kafka-retention");
		assertThat(existingEventIds()).hasSize(1);
	}

	@Test
	void deletePredicateRechecksStateAtDatabaseMutationTime() {
		String eventId = insertLedger("COMMITTED", CUTOFF.minus(Duration.ofDays(1)), null);
		RankingLedgerCleanupRepository repository = new RankingLedgerCleanupRepository(jdbc);
		jdbc.update("update ranking_event_ledger set state = 'REDIS_APPLIED' where event_id = ?", eventId);

		int deleted = repository.deleteIfStillEligible(eventId, Timestamp.from(CUTOFF));

		assertThat(deleted).isZero();
		assertThat(existingEventIds()).containsExactly(eventId);
	}

	private RankingLedgerCleanup cleanup(int batchSize) {
		RankingLedgerRetentionProperties properties = new RankingLedgerRetentionProperties(
				true,
				Duration.ofDays(30),
				Duration.ofDays(30),
				Duration.ofDays(30),
				Duration.ofDays(30),
				Duration.ofDays(30),
				batchSize,
				Duration.ofHours(1));
		return new RankingLedgerCleanup(
				new RankingLedgerCleanupRepository(jdbc),
				new RankingLedgerRetentionPolicy(properties, Clock.fixed(NOW, ZoneOffset.UTC)),
				properties);
	}

	private String insertLedger(String state, Instant committedAt, String rebuildRunId) {
		String eventId = UUID.randomUUID().toString();
		jdbc.update("insert into ranking_event_ledger(event_id, event_type, payload_fingerprint, state, source, "
					+ "rebuild_run_id, reserved_at, redis_applied_at, committed_at) values (?,?,?,?,?,?,now(6),?,?)",
				eventId,
				"order.completed",
				"a".repeat(64),
				state,
				rebuildRunId == null ? "NORMAL_CONSUMER" : "REBUILD",
				rebuildRunId,
				"REDIS_APPLIED".equals(state) ? Timestamp.from(NOW.minus(Duration.ofDays(31))) : null,
				committedAt == null ? null : Timestamp.from(committedAt));
		return eventId;
	}

	private String insertRebuildLedger(String rebuildState) {
		String runId = UUID.randomUUID().toString();
		jdbc.update("insert into ranking_rebuild_run(run_id, state, namespace, window_start_date, "
					+ "window_end_date, created_at, completed_at) values (?,?,?,current_date,current_date,now(6),?)",
				runId,
				rebuildState,
				"issue-125-" + runId,
				"COMPLETED".equals(rebuildState) ? Timestamp.from(NOW) : null);
		return insertLedger("COMMITTED", CUTOFF.minus(Duration.ofDays(1)), runId);
	}

	private java.util.List<String> existingEventIds() {
		return jdbc.queryForList("select event_id from ranking_event_ledger order by event_id", String.class);
	}
}
