// normal consumer와 DLT replay의 ranking event 단건 상태 전이를 MySQL에 기록합니다.
package com.example.coffeeordersystem.ranking.consumer;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RankingEventLedger {

	private static final String EVENT_TYPE = "order.completed";
	private static final String RESERVED = "RESERVED";
	private static final String REDIS_APPLIED = "REDIS_APPLIED";
	private static final String COMMITTED = "COMMITTED";

	private final JdbcTemplate jdbc;

	public RankingEventLedger(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Reservation reserve(String eventId, String fingerprint, RankingEventSource source) {
		return reserveInternal(eventId, fingerprint, source, false);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Reservation reserveReplay(String eventId, String fingerprint) {
		return reserveInternal(eventId, fingerprint, RankingEventSource.DLT_REPLAY, true);
	}

	private Reservation reserveInternal(
			String eventId,
			String fingerprint,
			RankingEventSource source,
			boolean allowDltInsert) {
		LedgerRow existing = jdbc.query(
				"select payload_fingerprint, state from ranking_event_ledger where event_id = ? for update",
				(resultSet, rowNum) -> new LedgerRow(
						resultSet.getString("payload_fingerprint"), resultSet.getString("state")),
				eventId).stream().findFirst().orElse(null);
		if (existing != null) {
			if (!existing.fingerprint().equals(fingerprint)) {
				throw new RankingEventPayloadConflictException(eventId);
			}
			return new Reservation(COMMITTED.equals(existing.state()), REDIS_APPLIED.equals(existing.state()));
		}
		if (source == RankingEventSource.DLT_REPLAY && !allowDltInsert) {
			throw new IllegalStateException("DLT_SOURCE_NOT_RESERVED eventId=" + eventId);
		}

		jdbc.update("insert into ranking_event_ledger(event_id, event_type, payload_fingerprint, state, source, "
					+ "reserved_at) values (?,?,?,?,?,?)",
				eventId, EVENT_TYPE, fingerprint, RESERVED, source.name(), Timestamp.valueOf(LocalDateTime.now()));
		return new Reservation(false, false);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markRedisApplied(String eventId) {
		int updated = jdbc.update("update ranking_event_ledger set state = ?, redis_applied_at = ? "
					+ "where event_id = ? and state = ?",
				REDIS_APPLIED, Timestamp.valueOf(LocalDateTime.now()), eventId, RESERVED);
		if (updated != 1) {
			throw new IllegalStateException("ranking ledger를 REDIS_APPLIED로 전환하지 못했습니다 eventId=" + eventId);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markCommitted(String eventId) {
		int updated = jdbc.update("update ranking_event_ledger set state = ?, committed_at = ? "
					+ "where event_id = ? and state = ?",
				COMMITTED, Timestamp.valueOf(LocalDateTime.now()), eventId, REDIS_APPLIED);
		if (updated != 1) {
			throw new IllegalStateException("ranking ledger를 COMMITTED로 전환하지 못했습니다 eventId=" + eventId);
		}
	}

	public record Reservation(boolean committed, boolean redisApplied) {
	}

	private record LedgerRow(String fingerprint, String state) {
	}
}
