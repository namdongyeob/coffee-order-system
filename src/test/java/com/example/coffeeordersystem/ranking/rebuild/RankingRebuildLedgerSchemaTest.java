package com.example.coffeeordersystem.ranking.rebuild;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class RankingRebuildLedgerSchemaTest {

	@Test
	void migrationDefinesLedgerAndPendingRebuildEventTables() throws IOException {
		String migration;
		try (InputStream input = getClass().getResourceAsStream(
				"/db/migration/V6__create_ranking_event_ledger.sql")) {
			assertThat(input).as("V6 ranking ledger migration").isNotNull();
			migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}

		assertThat(migration)
				.contains("create table ranking_event_ledger")
				.contains("payload_fingerprint")
				.contains("rebuild_run_id")
				.contains("create table ranking_rebuild_run")
				.contains("SWAPPED_PENDING_LEDGER")
				.contains("create table ranking_rebuild_run_event");
	}
}
