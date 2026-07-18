package com.example.coffeeordersystem.ranking.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.example.coffeeordersystem.TestcontainersConfiguration;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

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
		assertThat(properties.enabled()).isFalse();
		assertThat(properties.ledgerRetention()).isEqualTo(Duration.ofDays(30));
		assertThat(properties.redisMarkerTtl()).isEqualTo(Duration.ofDays(30));
		assertThat(properties.kafkaRetention()).isNull();
		assertThat(properties.dltRetention()).isNull();
		assertThat(properties.maximumRebuildRecoveryWindow()).isNull();
		assertThat(properties.batchSize()).isEqualTo(100);
		assertThat(properties.fixedDelay()).isEqualTo(Duration.ofHours(1));
		assertThat(cleanup).isNotNull();
		assertThat(context.getBeansOfType(RankingLedgerCleanupScheduler.class)).isEmpty();
	}

	@Test
	void enabledCleanupWithoutExplicitExternalWindowsFailsApplicationContext() {
		new ApplicationContextRunner()
				.withUserConfiguration(RankingLedgerRetentionConfiguration.class)
				.withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
				.withPropertyValues(
						"ranking.ledger.cleanup.enabled=true",
						"ranking.ledger.cleanup.ledger-retention=30d",
						"ranking.ledger.cleanup.redis-marker-ttl=30d",
						"ranking.ledger.cleanup.batch-size=100",
						"ranking.ledger.cleanup.fixed-delay=1h")
				.run(context -> {
					assertThat(context).hasFailed();
					assertThat(context.getStartupFailure())
							.hasRootCauseInstanceOf(IllegalStateException.class)
							.hasRootCauseMessage("ranking.ledger.cleanup.kafka-retention must be a known positive duration");
				});
	}
}
