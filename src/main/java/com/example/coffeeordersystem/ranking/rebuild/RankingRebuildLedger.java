// rebuild swap, offset 이동, ledger backfill의 재실행 상태를 DB에 보존합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class RankingRebuildLedger {
	static final String PREPARED = "PREPARED";
	static final String SWAPPED_PENDING_OFFSET = "SWAPPED_PENDING_OFFSET";
	static final String OFFSET_APPLIED_PENDING_LEDGER = "OFFSET_APPLIED_PENDING_LEDGER";
	static final String RECOVERY_REQUIRED = "RECOVERY_REQUIRED";
	static final String COMPLETED = "COMPLETED";

	private static final String EVENT_TYPE = "order.completed";
	private static final String SOURCE = "REBUILD";
	private static final String EVENT_COMMITTED = "COMMITTED";
	private static final int BACKFILL_BATCH_SIZE = 50;

	private final JdbcTemplate jdbc;

	RankingRebuildLedger(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	void prepare(
			UUID runId,
			Collection<RankingRebuildEvent> events,
			String namespace,
			Set<String> dates,
			Map<TopicPartition, Long> capturedEnds,
			RankingRebuildOffsetManager.OffsetSnapshot previousOffsets,
			Runnable heartbeat) {
		List<RankingRebuildEvent> eventList = List.copyOf(events);
		for (RankingRebuildEvent event : eventList) {
			validateExistingFingerprint(event);
		}
		List<LocalDate> windowDates = dates.stream().map(LocalDate::parse).sorted().toList();
		LocalDateTime now = LocalDateTime.now();
		jdbc.update("insert into ranking_rebuild_run(run_id, state, namespace, window_start_date, window_end_date, "
				+ "created_at) values (?,?,?,?,?,?)",
				runId.toString(), PREPARED, namespace, windowDates.get(0), windowDates.get(windowDates.size() - 1),
				Timestamp.valueOf(now));
		for (int start = 0; start < eventList.size(); start += BACKFILL_BATCH_SIZE) {
			heartbeat.run();
			int end = Math.min(start + BACKFILL_BATCH_SIZE, eventList.size());
			List<RankingRebuildEvent> batch = eventList.subList(start, end);
			jdbc.batchUpdate(
					"insert into ranking_rebuild_run_event(run_id, event_id, event_type, order_id, user_id, menu_id, "
							+ "paid_amount, ordered_at, payload_fingerprint) values (?,?,?,?,?,?,?,?,?)",
					batch,
					batch.size(),
					(PreparedStatement statement, RankingRebuildEvent event) -> {
						statement.setString(1, runId.toString());
						statement.setString(2, event.eventId().toString());
						statement.setString(3, EVENT_TYPE);
						statement.setLong(4, event.orderId());
						statement.setLong(5, event.userId());
						statement.setLong(6, event.menuId());
						statement.setInt(7, event.paidAmount());
						statement.setTimestamp(8, Timestamp.valueOf(event.orderedAt()));
						statement.setString(9, event.payloadFingerprint());
					});
		}
		heartbeat.run();
		jdbc.batchUpdate(
				"insert into ranking_rebuild_run_offset(run_id, topic, partition_id, captured_end, previous_present, "
						+ "previous_offset) values (?,?,?,?,?,?)",
				capturedEnds.entrySet(),
				capturedEnds.size(),
				(PreparedStatement statement, Map.Entry<TopicPartition, Long> entry) -> {
					Optional<OffsetAndMetadata> previous = previousOffsets.values()
							.getOrDefault(entry.getKey(), Optional.empty());
					statement.setString(1, runId.toString());
					statement.setString(2, entry.getKey().topic());
					statement.setInt(3, entry.getKey().partition());
					statement.setLong(4, entry.getValue());
					statement.setBoolean(5, previous.isPresent());
					if (previous.isPresent()) {
						statement.setLong(6, previous.orElseThrow().offset());
					} else {
						statement.setNull(6, java.sql.Types.BIGINT);
					}
				});
	}

	Optional<RunPlan> findIncomplete() {
		List<RunPlan> runs = jdbc.query(
				"select run_id, state, namespace, window_start_date, window_end_date from ranking_rebuild_run "
						+ "where state <> ? order by created_at",
				(resultSet, rowNum) -> runPlan(resultSet), COMPLETED);
		if (runs.size() > 1) {
			throw new RankingRebuildException("미완료 rebuild run이 둘 이상이라 자동 복구할 수 없습니다");
		}
		return runs.stream().findFirst();
	}

	void markSwapped(UUID runId) {
		transition(runId, PREPARED, SWAPPED_PENDING_OFFSET, "swapped_at", "swap 후 offset pending 상태로 전환하지 못했습니다");
	}

	void markOffsetsApplied(UUID runId) {
		transition(runId, SWAPPED_PENDING_OFFSET, OFFSET_APPLIED_PENDING_LEDGER, "offsets_applied_at",
				"offset 적용 후 ledger pending 상태로 전환하지 못했습니다");
	}

	void markRecoveryRequired(UUID runId) {
		int updated = jdbc.update("update ranking_rebuild_run set state = ? where run_id = ? and state <> ?",
				RECOVERY_REQUIRED, runId.toString(), COMPLETED);
		if (updated != 1) {
			throw new RankingRebuildException("불완전 보상 run을 RECOVERY_REQUIRED로 보존하지 못했습니다");
		}
	}

	void backfillAndComplete(UUID runId) {
		backfillAndComplete(runId, () -> { });
	}

	void backfillAndComplete(UUID runId, Runnable heartbeat) {
		List<StoredEvent> events = jdbc.query(
				"select event_id, order_id, user_id, menu_id, paid_amount, ordered_at, payload_fingerprint "
						+ "from ranking_rebuild_run_event where run_id = ? order by event_id",
				storedEventMapper(), runId.toString());
		for (int start = 0; start < events.size(); start += BACKFILL_BATCH_SIZE) {
			heartbeat.run();
			int end = Math.min(start + BACKFILL_BATCH_SIZE, events.size());
			for (StoredEvent event : events.subList(start, end)) {
				backfillEvent(runId, event);
			}
		}
		heartbeat.run();
		int updated = jdbc.update("update ranking_rebuild_run set state = ?, completed_at = ? "
				+ "where run_id = ? and state = ?",
				COMPLETED, Timestamp.valueOf(LocalDateTime.now()), runId.toString(), OFFSET_APPLIED_PENDING_LEDGER);
		if (updated != 1) {
			throw new RankingRebuildException("pending rebuild run을 완료 상태로 전환하지 못했습니다");
		}
	}

	void cancel(UUID runId) {
		int deleted = jdbc.update("delete from ranking_rebuild_run where run_id = ? and state <> ?",
				runId.toString(), COMPLETED);
		if (deleted != 1) {
			throw new RankingRebuildException("보상된 rebuild run을 원자 삭제하지 못했습니다");
		}
	}

	private RunPlan runPlan(ResultSet resultSet) throws java.sql.SQLException {
		UUID runId = UUID.fromString(resultSet.getString("run_id"));
		List<OffsetRow> offsetRows = jdbc.query(
				"select topic, partition_id, captured_end, previous_present, previous_offset "
						+ "from ranking_rebuild_run_offset where run_id = ? order by topic, partition_id",
				(result, rowNum) -> new OffsetRow(
						new TopicPartition(result.getString("topic"), result.getInt("partition_id")),
						result.getLong("captured_end"), result.getBoolean("previous_present"),
						result.getObject("previous_offset", Long.class)),
				runId.toString());
		Map<TopicPartition, OffsetAndMetadata> targets = offsetRows.stream().collect(Collectors.toMap(
				OffsetRow::partition, row -> new OffsetAndMetadata(row.capturedEnd())));
		Map<TopicPartition, Optional<OffsetAndMetadata>> previous = offsetRows.stream().collect(Collectors.toMap(
				OffsetRow::partition,
				row -> row.previousPresent()
						? Optional.of(new OffsetAndMetadata(row.previousOffset()))
						: Optional.empty()));
		Set<String> dates = new LinkedHashSet<>();
		LocalDate date = resultSet.getDate("window_start_date").toLocalDate();
		LocalDate end = resultSet.getDate("window_end_date").toLocalDate();
		while (!date.isAfter(end)) {
			dates.add(date.toString());
			date = date.plusDays(1);
		}
		return new RunPlan(runId, resultSet.getString("state"), resultSet.getString("namespace"),
				Set.copyOf(dates), Map.copyOf(targets),
				new RankingRebuildOffsetManager.OffsetSnapshot(Map.copyOf(previous)));
	}

	private void transition(UUID runId, String from, String to, String timestampColumn, String failureMessage) {
		int updated = jdbc.update("update ranking_rebuild_run set state = ?, " + timestampColumn + " = ? "
				+ "where run_id = ? and state = ?",
				to, Timestamp.valueOf(LocalDateTime.now()), runId.toString(), from);
		if (updated != 1) {
			throw new RankingRebuildException(failureMessage);
		}
	}

	private void backfillEvent(UUID runId, StoredEvent stored) {
		RankingRebuildEvent event = stored.event();
		LedgerRow existing = jdbc.query(
				"select payload_fingerprint, state from ranking_event_ledger where event_id = ? for update",
				ledgerRowMapper(), event.eventId().toString()).stream().findFirst().orElse(null);
		LocalDateTime now = LocalDateTime.now();
		if (existing == null) {
			jdbc.update("insert into ranking_event_ledger(event_id, event_type, payload_fingerprint, state, source, "
					+ "rebuild_run_id, reserved_at, committed_at) values (?,?,?,?,?,?,?,?)",
					event.eventId().toString(), EVENT_TYPE, stored.payloadFingerprint(), EVENT_COMMITTED, SOURCE,
					runId.toString(), Timestamp.valueOf(now), Timestamp.valueOf(now));
			return;
		}
		if (!existing.payloadFingerprint().equals(stored.payloadFingerprint())) {
			throw new RankingRebuildException("EVENT_ID_PAYLOAD_CONFLICT eventId=" + event.eventId());
		}
		if (!EVENT_COMMITTED.equals(existing.state())) {
			jdbc.update("update ranking_event_ledger set state = ?, committed_at = ? where event_id = ?",
					EVENT_COMMITTED, Timestamp.valueOf(now), event.eventId().toString());
		}
	}

	private void validateExistingFingerprint(RankingRebuildEvent event) {
		String existing = jdbc.query(
				"select payload_fingerprint from ranking_event_ledger where event_id = ?",
				(resultSet, rowNum) -> resultSet.getString("payload_fingerprint"),
				event.eventId().toString()).stream().findFirst().orElse(null);
		if (existing != null && !existing.equals(event.payloadFingerprint())) {
			throw new RankingRebuildException("EVENT_ID_PAYLOAD_CONFLICT eventId=" + event.eventId());
		}
	}

	private RowMapper<StoredEvent> storedEventMapper() {
		return (ResultSet resultSet, int rowNum) -> new StoredEvent(
				new RankingRebuildEvent(
						UUID.fromString(resultSet.getString("event_id")),
						resultSet.getLong("order_id"), resultSet.getLong("user_id"), resultSet.getLong("menu_id"),
						resultSet.getInt("paid_amount"), resultSet.getTimestamp("ordered_at").toLocalDateTime()),
				resultSet.getString("payload_fingerprint"));
	}

	private RowMapper<LedgerRow> ledgerRowMapper() {
		return (ResultSet resultSet, int rowNum) -> new LedgerRow(
				resultSet.getString("payload_fingerprint"), resultSet.getString("state"));
	}

	record RunPlan(
			UUID runId,
			String state,
			String namespace,
			Set<String> dates,
			Map<TopicPartition, OffsetAndMetadata> targetOffsets,
			RankingRebuildOffsetManager.OffsetSnapshot previousOffsets) {
	}

	private record OffsetRow(
			TopicPartition partition, long capturedEnd, boolean previousPresent, Long previousOffset) {
	}

	private record LedgerRow(String payloadFingerprint, String state) {
	}

	private record StoredEvent(RankingRebuildEvent event, String payloadFingerprint) {
	}
}
