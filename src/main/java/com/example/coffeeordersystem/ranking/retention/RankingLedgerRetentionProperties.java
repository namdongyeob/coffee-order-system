package com.example.coffeeordersystem.ranking.retention;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ranking.ledger.cleanup")
public record RankingLedgerRetentionProperties(
		boolean enabled,
		Duration ledgerRetention,
		Duration redisMarkerTtl,
		Duration kafkaRetention,
		Duration dltRetention,
		Duration maximumRebuildRecoveryWindow,
		int batchSize,
		Duration fixedDelay) {

	public static final int MAX_BATCH_SIZE = 1_000;

	public void validate() {
		requirePositive("ledger-retention", ledgerRetention);
		requirePositive("redis-marker-ttl", redisMarkerTtl);
		requirePositive("kafka-retention", kafkaRetention);
		requirePositive("dlt-retention", dltRetention);
		requirePositive("maximum-rebuild-recovery-window", maximumRebuildRecoveryWindow);
		requirePositive("fixed-delay", fixedDelay);
		if (batchSize < 1 || batchSize > MAX_BATCH_SIZE) {
			throw new IllegalStateException(
					"ranking.ledger.cleanup.batch-size must be between 1 and " + MAX_BATCH_SIZE);
		}
		if (redisMarkerTtl.compareTo(ledgerRetention) < 0) {
			throw new IllegalStateException(
					"ranking.ledger.cleanup.redis-marker-ttl must be >= ledger-retention");
		}
		requireCovered("kafka-retention", kafkaRetention);
		requireCovered("dlt-retention", dltRetention);
		requireCovered("maximum-rebuild-recovery-window", maximumRebuildRecoveryWindow);
	}

	private void requireCovered(String name, Duration protectedWindow) {
		if (protectedWindow.compareTo(ledgerRetention) > 0) {
			throw new IllegalStateException(
					"ranking.ledger.cleanup." + name + " must be <= ledger-retention");
		}
	}

	private static void requirePositive(String name, Duration value) {
		if (value == null || value.isZero() || value.isNegative()) {
			throw new IllegalStateException("ranking.ledger.cleanup." + name + " must be a known positive duration");
		}
	}
}
