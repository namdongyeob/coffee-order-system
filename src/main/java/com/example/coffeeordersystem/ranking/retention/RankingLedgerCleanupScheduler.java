package com.example.coffeeordersystem.ranking.retention;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ranking.ledger.cleanup.enabled", havingValue = "true")
@Slf4j
public class RankingLedgerCleanupScheduler {

	private final RankingLedgerCleanup cleanup;

	public RankingLedgerCleanupScheduler(RankingLedgerCleanup cleanup) {
		this.cleanup = cleanup;
	}

	@Scheduled(fixedDelayString = "${ranking.ledger.cleanup.fixed-delay}")
	void runScheduled() {
		int deleted = cleanup.cleanupOneBatch();
		log.info("ranking_ledger_cleanup_completed deleted={}", deleted);
	}
}
