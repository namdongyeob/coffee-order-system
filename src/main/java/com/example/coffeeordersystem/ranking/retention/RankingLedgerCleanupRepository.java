package com.example.coffeeordersystem.ranking.retention;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

class RankingLedgerCleanupRepository {

	static final String CANDIDATE_SQL = "select ledger.event_id from ranking_event_ledger ledger "
			+ "force index (idx_ranking_event_ledger_cleanup) "
			+ "left join ranking_rebuild_run rebuild on rebuild.run_id = ledger.rebuild_run_id "
			+ "where ledger.state = ? and ledger.committed_at is not null "
			+ "and ledger.committed_at < ? "
			+ "and (ledger.rebuild_run_id is null or rebuild.state = ?) "
			+ "order by ledger.committed_at, ledger.event_id limit ? for update skip locked";

	private static final String COMMITTED = "COMMITTED";
	private static final String REBUILD_COMPLETED = "COMPLETED";

	private final JdbcTemplate jdbc;

	RankingLedgerCleanupRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	List<String> lockEligibleBefore(Timestamp cutoff, int batchSize) {
		return jdbc.queryForList(
				CANDIDATE_SQL,
				String.class,
				COMMITTED,
				cutoff,
				REBUILD_COMPLETED,
				batchSize);
	}

	int deleteIfStillEligible(String eventId, Timestamp cutoff) {
		return jdbc.update(
				"delete ledger from ranking_event_ledger ledger "
						+ "left join ranking_rebuild_run rebuild on rebuild.run_id = ledger.rebuild_run_id "
						+ "where ledger.event_id = ? and ledger.state = ? "
						+ "and ledger.committed_at is not null and ledger.committed_at < ? "
						+ "and (ledger.rebuild_run_id is null or rebuild.state = ?)",
				eventId,
				COMMITTED,
				cutoff,
				REBUILD_COMPLETED);
	}
}
