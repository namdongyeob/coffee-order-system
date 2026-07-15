// maintenance rebuild 실행 중 일반 Kafka consumer가 등록되지 않는지 검증합니다.
package com.example.coffeeordersystem.ranking.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class RankingEventConsumerConditionTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(RankingEventConsumer.class, TestConfiguration.class);

	@Test
	void doesNotRegisterNormalConsumerWhenMaintenanceRebuildRunnerIsEnabled() {
		contextRunner.withPropertyValues("ranking.rebuild.enabled=true")
				.run(context -> assertThat(context).doesNotHaveBean(RankingEventConsumer.class));
	}

	@Test
	void registersNormalConsumerWhenMaintenanceRebuildRunnerIsNotEnabled() {
		contextRunner.run(context -> assertThat(context).hasSingleBean(RankingEventConsumer.class));
	}

	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

		@Bean
		RankingEventProcessor rankingEventProcessor() {
			return mock(RankingEventProcessor.class);
		}
	}
}
