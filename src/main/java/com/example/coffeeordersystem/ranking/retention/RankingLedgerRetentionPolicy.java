package com.example.coffeeordersystem.ranking.retention;

import java.time.Clock;
import java.time.Instant;

public class RankingLedgerRetentionPolicy {

	private final RankingLedgerRetentionProperties properties;
	private final Clock clock;

	public RankingLedgerRetentionPolicy(RankingLedgerRetentionProperties properties, Clock clock) {
		if (properties.enabled()) {
			properties.validate();
		}
		this.properties = properties;
		this.clock = clock;
	}

	public Instant cutoff() {
		return clock.instant().minus(properties.ledgerRetention());
	}
}
