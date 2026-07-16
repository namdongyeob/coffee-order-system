// Spring test context가 같은 Testcontainers 인스턴스를 공유하는 계약을 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SharedTestcontainersTest {

	@Test
	void exposesOneSharedContainerInstanceForEveryTestContext() {
		TestcontainersConfiguration configuration = new TestcontainersConfiguration();

		assertThat(configuration.kafkaContainer()).isSameAs(SharedTestcontainers.kafka());
		assertThat(configuration.mysqlContainer()).isSameAs(SharedTestcontainers.mysql());
		assertThat(configuration.redisContainer()).isSameAs(SharedTestcontainers.redis());
	}
}
