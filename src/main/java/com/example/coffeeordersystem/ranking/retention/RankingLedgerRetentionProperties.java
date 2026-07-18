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
	private static final Duration MINIMUM_REDIS_MARKER_TTL = Duration.ofSeconds(1);

	public void validate() {
		requirePositive("ledger-retention", ledgerRetention);
		requirePositive("redis-marker-ttl", redisMarkerTtl);
		requirePositive("fixed-delay", fixedDelay);
		if (redisMarkerTtl.compareTo(MINIMUM_REDIS_MARKER_TTL) < 0) {
			throw new IllegalStateException(
					"ranking.ledger.cleanup.redis-marker-ttl must be at least 1s");
		}
		if (batchSize < 1 || batchSize > MAX_BATCH_SIZE) {
			throw new IllegalStateException(
					"ranking.ledger.cleanup.batch-size must be between 1 and " + MAX_BATCH_SIZE);
		}
		Duration effectiveRedisMarkerTtl = Duration.ofSeconds(redisMarkerTtl.toSeconds());
		if (effectiveRedisMarkerTtl.compareTo(ledgerRetention) < 0) {
			throw new IllegalStateException(
					"ranking.ledger.cleanup.redis-marker-ttl after EX seconds conversion must be >= ledger-retention");
		}
		if (!enabled) {
			return;
		}
		requirePositive("kafka-retention", kafkaRetention);
		requirePositive("dlt-retention", dltRetention);
		requirePositive("maximum-rebuild-recovery-window", maximumRebuildRecoveryWindow);
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
