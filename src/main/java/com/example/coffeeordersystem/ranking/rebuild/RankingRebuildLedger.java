// rebuild swap과 ledger backfill의 상태 및 재실행 이벤트를 DB에 보존합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class RankingRebuildLedger {
	private static final String EVENT_TYPE = "order.completed";
	private static final String SOURCE = "REBUILD";
	private static final String PREPARED = "PREPARED";
	private static final String PENDING = "SWAPPED_PENDING_LEDGER";
	private static final String COMMITTED = "COMMITTED";

	private final JdbcTemplate jdbc;

	RankingRebuildLedger(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	void prepare(UUID runId, Collection<RankingRebuildEvent> events) {
		for (RankingRebuildEvent event : events) {
			validateExistingFingerprint(event);
		}
		LocalDateTime now = LocalDateTime.now();
		jdbc.update("insert into ranking_rebuild_run(run_id, state, created_at) values (?,?,?)",
				runId.toString(), PREPARED, Timestamp.valueOf(now));
		if (events.isEmpty()) {
			return;
		}
		jdbc.batchUpdate(
				"insert into ranking_rebuild_run_event(run_id, event_id, event_type, order_id, user_id, menu_id, "
						+ "paid_amount, ordered_at, payload_fingerprint) values (?,?,?,?,?,?,?,?,?)",
					events,
					events.size(),
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

	void markSwapped(UUID runId) {
		int updated = jdbc.update("update ranking_rebuild_run set state = ?, swapped_at = ? "
				+ "where run_id = ? and state = ?",
				PENDING, Timestamp.valueOf(LocalDateTime.now()), runId.toString(), PREPARED);
		if (updated != 1) {
			throw new RankingRebuildException("swap 후 pending ledger 상태로 전환하지 못했습니다");
		}
	}

	void backfillAndComplete(UUID runId) {
		List<StoredEvent> events = jdbc.query(
				"select event_id, order_id, user_id, menu_id, paid_amount, ordered_at, payload_fingerprint "
						+ "from ranking_rebuild_run_event where run_id = ? order by event_id",
				storedEventMapper(), runId.toString());
		for (StoredEvent event : events) {
			backfillEvent(runId, event);
		}
		int updated = jdbc.update("update ranking_rebuild_run set state = ?, completed_at = ? "
				+ "where run_id = ? and state = ?",
				"COMPLETED", Timestamp.valueOf(LocalDateTime.now()), runId.toString(), PENDING);
		if (updated != 1) {
			throw new RankingRebuildException("pending rebuild run을 완료 상태로 전환하지 못했습니다");
		}
	}

	boolean recoverPending() {
		List<String> runIds = jdbc.queryForList(
				"select run_id from ranking_rebuild_run where state = ? order by created_at", String.class, PENDING);
		for (String runId : runIds) {
			backfillAndComplete(UUID.fromString(runId));
		}
		return !runIds.isEmpty();
	}

	void discard(UUID runId) {
		jdbc.update("delete from ranking_rebuild_run_event where run_id = ?", runId.toString());
		jdbc.update("delete from ranking_rebuild_run where run_id = ? and state = ?", runId.toString(), PREPARED);
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
					event.eventId().toString(), EVENT_TYPE, stored.payloadFingerprint(), COMMITTED, SOURCE,
					runId.toString(), Timestamp.valueOf(now), Timestamp.valueOf(now));
			return;
		}
		if (!existing.payloadFingerprint().equals(stored.payloadFingerprint())) {
			throw new RankingRebuildException("EVENT_ID_PAYLOAD_CONFLICT eventId=" + event.eventId());
		}
		if (!COMMITTED.equals(existing.state())) {
			jdbc.update("update ranking_event_ledger set state = ?, committed_at = ? where event_id = ?",
					COMMITTED, Timestamp.valueOf(now), event.eventId().toString());
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

	private record LedgerRow(String payloadFingerprint, String state) {
	}

	private record StoredEvent(RankingRebuildEvent event, String payloadFingerprint) {
	}
}
