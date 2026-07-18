package com.example.coffeeordersystem.ranking.retention;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class RankingLedgerCleanup {

	private final RankingLedgerCleanupRepository repository;
	private final RankingLedgerRetentionPolicy policy;
	private final RankingLedgerRetentionProperties properties;

	RankingLedgerCleanup(
			RankingLedgerCleanupRepository repository,
			RankingLedgerRetentionPolicy policy,
			RankingLedgerRetentionProperties properties) {
		this.repository = repository;
		this.policy = policy;
		this.properties = properties;
	}

	@Transactional
	public int cleanupOneBatch() {
		properties.validate();
		Instant cutoff = policy.cutoff();
		Timestamp cutoffTimestamp = Timestamp.from(cutoff);
		List<String> candidates = repository.lockEligibleBefore(cutoffTimestamp, properties.batchSize());

		int deleted = 0;
		for (String eventId : candidates) {
			deleted += repository.deleteIfStillEligible(eventId, cutoffTimestamp);
		}
		return deleted;
	}
}
