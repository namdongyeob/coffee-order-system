// Spring context 종료 시 공유 Testcontainers가 닫히지 않는 경계를 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.lifecycle.TestcontainersLifecycleApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class SharedTestcontainersLifecycleIntegrationTest {

	@Test
	void closingOneTestContextDoesNotCloseSharedContainers() {
		try (AnnotationConfigApplicationContext context = newContext()) {
			assertThat(SharedTestcontainers.kafka().isRunning()).isTrue();
			assertThat(SharedTestcontainers.mysql().isRunning()).isTrue();
			assertThat(SharedTestcontainers.redis().isRunning()).isTrue();
		}

		assertThat(SharedTestcontainers.kafka().isRunning()).isTrue();
		assertThat(SharedTestcontainers.mysql().isRunning()).isTrue();
		assertThat(SharedTestcontainers.redis().isRunning()).isTrue();

		try (AnnotationConfigApplicationContext restarted = newContext()) {
			assertThat(SharedTestcontainers.kafka().isRunning()).isTrue();
			assertThat(SharedTestcontainers.mysql().isRunning()).isTrue();
			assertThat(SharedTestcontainers.redis().isRunning()).isTrue();
		}
	}

	private AnnotationConfigApplicationContext newContext() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		new TestcontainersLifecycleApplicationContextInitializer().initialize(context);
		context.register(TestcontainersConfiguration.class);
		context.refresh();
		return context;
	}
}
