package com.example.coffeeordersystem.ranking.retention;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RankingLedgerRetentionPropertiesTest {

	@Test
	void rejectsNonPositiveDurationsUnsafeBatchAndShorterProtectionWindows() {
		assertThatThrownBy(() -> properties(Duration.ZERO, Duration.ofDays(30), 100).validate())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("ledger-retention");
		assertThatThrownBy(() -> properties(Duration.ofDays(30), Duration.ofDays(29), 100).validate())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("redis-marker-ttl");
		assertThatThrownBy(() -> properties(Duration.ofDays(30), Duration.ofDays(30), 1_001).validate())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("batch-size");
		assertThatThrownBy(() -> propertiesWithKafkaRetention(Duration.ofDays(31)).validate())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("kafka-retention");
	}

	@Test
	void acceptsKnownProtectionWindowsThatDoNotExceedLedgerRetention() {
		assertThatCode(() -> properties(Duration.ofDays(30), Duration.ofDays(30), 100).validate())
				.doesNotThrowAnyException();
	}

	private RankingLedgerRetentionProperties properties(
			Duration ledgerRetention,
			Duration markerTtl,
			int batchSize) {
		return new RankingLedgerRetentionProperties(
				true,
				ledgerRetention,
				markerTtl,
				Duration.ofDays(30),
				Duration.ofDays(30),
				Duration.ofDays(30),
				batchSize,
				Duration.ofHours(1));
	}

	private RankingLedgerRetentionProperties propertiesWithKafkaRetention(Duration kafkaRetention) {
		return new RankingLedgerRetentionProperties(
				true,
				Duration.ofDays(30),
				Duration.ofDays(30),
				kafkaRetention,
				Duration.ofDays(30),
				Duration.ofDays(30),
				100,
				Duration.ofHours(1));
	}
}
