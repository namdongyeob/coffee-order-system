package com.example.coffeeordersystem.ranking.retention;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RankingLedgerRetentionPolicyTest {

	@Test
	void calculatesOneFixedCutoffFromExactlyOneClockRead() {
		Instant now = Instant.parse("2026-07-18T04:00:00Z");
		CountingClock clock = new CountingClock(now);
		RankingLedgerRetentionPolicy policy = new RankingLedgerRetentionPolicy(properties(), clock);

		Instant cutoff = policy.cutoff();

		assertThat(cutoff).isEqualTo(Instant.parse("2026-06-18T04:00:00Z"));
		assertThat(clock.readCount()).isOne();
	}

	private RankingLedgerRetentionProperties properties() {
		return new RankingLedgerRetentionProperties(
				true,
				Duration.ofDays(30),
				Duration.ofDays(30),
				Duration.ofDays(30),
				Duration.ofDays(30),
				Duration.ofDays(30),
				100,
				Duration.ofHours(1));
	}

	private static final class CountingClock extends Clock {
		private final Instant instant;
		private final AtomicInteger reads = new AtomicInteger();

		private CountingClock(Instant instant) {
			this.instant = instant;
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			reads.incrementAndGet();
			return instant;
		}

		private int readCount() {
			return reads.get();
		}
	}
}
