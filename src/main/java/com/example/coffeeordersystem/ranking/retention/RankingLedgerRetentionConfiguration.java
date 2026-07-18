package com.example.coffeeordersystem.ranking.retention;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RankingLedgerRetentionProperties.class)
public class RankingLedgerRetentionConfiguration {

	@Bean
	@ConditionalOnMissingBean(Clock.class)
	Clock rankingLedgerRetentionClock() {
		return Clock.systemUTC();
	}

	@Bean
	RankingLedgerRetentionPolicy rankingLedgerRetentionPolicy(
			RankingLedgerRetentionProperties properties,
			Clock clock) {
		return new RankingLedgerRetentionPolicy(properties, clock);
	}

	@Bean
	RankingLedgerCleanupRepository rankingLedgerCleanupRepository(JdbcTemplate jdbc) {
		return new RankingLedgerCleanupRepository(jdbc);
	}

	@Bean
	RankingLedgerCleanup rankingLedgerCleanup(
			RankingLedgerCleanupRepository repository,
			RankingLedgerRetentionPolicy policy,
			RankingLedgerRetentionProperties properties) {
		return new RankingLedgerCleanup(repository, policy, properties);
	}
}
