package com.example.coffeeordersystem.ranking.retention;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
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

	@Test
	void disabledCleanupAllowsUnknownExternalWindowsButEnabledCleanupRejectsThem() {
		RankingLedgerRetentionProperties disabled = propertiesWithUnknownExternalWindows(false);
		RankingLedgerRetentionProperties enabled = propertiesWithUnknownExternalWindows(true);

		assertThatCode(() -> new RankingLedgerRetentionPolicy(disabled, Clock.systemUTC()))
				.doesNotThrowAnyException();
		assertThatThrownBy(() -> new RankingLedgerRetentionPolicy(enabled, Clock.systemUTC()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("kafka-retention");
	}

	@Test
	void disabledCleanupStillRejectsUnsafeCoreRetentionSettings() {
		RankingLedgerRetentionProperties shorterMarker = propertiesWithCoreSettings(
				Duration.ofDays(30), Duration.ofDays(29));
		RankingLedgerRetentionProperties zeroMarker = propertiesWithCoreSettings(
				Duration.ofDays(30), Duration.ZERO);

		assertThatThrownBy(() -> new RankingLedgerRetentionPolicy(shorterMarker, Clock.systemUTC()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("redis-marker-ttl");
		assertThatThrownBy(() -> new RankingLedgerRetentionPolicy(zeroMarker, Clock.systemUTC()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("redis-marker-ttl");
	}

	@Test
	void rejectsSubSecondMarkerTtlBeforeRedisCanReceiveExZero() {
		RankingLedgerRetentionProperties subSecondMarker = propertiesWithCoreSettings(
				Duration.ofMillis(500), Duration.ofMillis(500));

		assertThatThrownBy(() -> new RankingLedgerRetentionPolicy(subSecondMarker, Clock.systemUTC()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("redis-marker-ttl must be at least 1s");
	}

	@Test
	void rejectsMarkerWhoseEffectiveRedisExTtlIsShorterThanLedgerRetention() {
		RankingLedgerRetentionProperties truncatedMarker = propertiesWithCoreSettings(
				Duration.ofMillis(1_500), Duration.ofMillis(1_500));

		assertThatThrownBy(() -> new RankingLedgerRetentionPolicy(truncatedMarker, Clock.systemUTC()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("after EX seconds conversion");
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

	private RankingLedgerRetentionProperties propertiesWithUnknownExternalWindows(boolean enabled) {
		return new RankingLedgerRetentionProperties(
				enabled,
				Duration.ofDays(30),
				Duration.ofDays(30),
				null,
				null,
				null,
				100,
				Duration.ofHours(1));
	}

	private RankingLedgerRetentionProperties propertiesWithCoreSettings(
			Duration ledgerRetention,
			Duration markerTtl) {
		return new RankingLedgerRetentionProperties(
				false,
				ledgerRetention,
				markerTtl,
				null,
				null,
				null,
				100,
				Duration.ofHours(1));
	}
}
