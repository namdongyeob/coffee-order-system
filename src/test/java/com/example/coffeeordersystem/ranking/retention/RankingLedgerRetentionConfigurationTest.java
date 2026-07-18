package com.example.coffeeordersystem.ranking.retention;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.TestcontainersConfiguration;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"ranking.consumer.enabled=false",
		"ranking.ledger.cleanup.enabled=false"
})
class RankingLedgerRetentionConfigurationTest {

	@Autowired RankingLedgerRetentionProperties properties;
	@Autowired RankingLedgerCleanup cleanup;
	@Autowired ApplicationContext context;

	@Test
	void bindsDocumentedDefaultsAndCreatesCleanupServiceWhenSchedulingIsDisabled() {
		assertThat(properties.ledgerRetention()).isEqualTo(Duration.ofDays(30));
		assertThat(properties.redisMarkerTtl()).isEqualTo(Duration.ofDays(30));
		assertThat(properties.kafkaRetention()).isEqualTo(Duration.ofDays(30));
		assertThat(properties.dltRetention()).isEqualTo(Duration.ofDays(30));
		assertThat(properties.maximumRebuildRecoveryWindow()).isEqualTo(Duration.ofDays(30));
		assertThat(properties.batchSize()).isEqualTo(100);
		assertThat(properties.fixedDelay()).isEqualTo(Duration.ofHours(1));
		assertThat(cleanup).isNotNull();
		assertThat(context.getBeansOfType(RankingLedgerCleanupScheduler.class)).isEmpty();
	}
}
