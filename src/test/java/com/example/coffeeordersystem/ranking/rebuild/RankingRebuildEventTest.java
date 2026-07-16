package com.example.coffeeordersystem.ranking.rebuild;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RankingRebuildEventTest {

	@Test
	void sameCorePayloadProducesTheSameSha256Fingerprint() {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0);
		RankingRebuildEvent first = new RankingRebuildEvent(UUID.randomUUID(), 1L, 2L, 3L, 4500, orderedAt);
		RankingRebuildEvent retry = new RankingRebuildEvent(UUID.randomUUID(), 1L, 2L, 3L, 4500, orderedAt);

		assertThat(first.payloadFingerprint()).hasSize(64).isEqualTo(retry.payloadFingerprint());
	}

	@Test
	void changedCorePayloadProducesADifferentFingerprint() {
		LocalDateTime orderedAt = LocalDateTime.of(2026, 7, 12, 10, 0);
		RankingRebuildEvent first = new RankingRebuildEvent(UUID.randomUUID(), 1L, 2L, 3L, 4500, orderedAt);
		RankingRebuildEvent changed = new RankingRebuildEvent(UUID.randomUUID(), 1L, 2L, 4L, 4500, orderedAt);

		assertThat(first.payloadFingerprint()).isNotEqualTo(changed.payloadFingerprint());
	}
}
