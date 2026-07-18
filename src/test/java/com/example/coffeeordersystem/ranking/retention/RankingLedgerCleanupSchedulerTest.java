package com.example.coffeeordersystem.ranking.retention;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class RankingLedgerCleanupSchedulerTest {

	@Test
	void oneSchedulerTickRunsExactlyOneBoundedBatch() {
		RankingLedgerCleanup cleanup = mock(RankingLedgerCleanup.class);
		RankingLedgerCleanupScheduler scheduler = new RankingLedgerCleanupScheduler(cleanup);

		scheduler.runScheduled();

		verify(cleanup).cleanupOneBatch();
	}
}
