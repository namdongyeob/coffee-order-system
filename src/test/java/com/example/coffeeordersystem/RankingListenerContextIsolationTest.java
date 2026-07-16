package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

class RankingListenerContextIsolationTest {

	@Test
	void kafkaRedisListenerContextIsClosedAfterClass() {
		assertAfterClass(RankingEventConsumerKafkaRedisIntegrationTest.class);
	}

	@Test
	void dltListenerContextIsClosedAfterClass() {
		assertAfterClass(RankingEventConsumerDltIntegrationTest.class);
	}

	private void assertAfterClass(Class<?> testClass) {
		DirtiesContext annotation = testClass.getAnnotation(DirtiesContext.class);

		assertThat(annotation).as(testClass.getSimpleName() + " must close its listener context").isNotNull();
		assertThat(annotation.classMode()).isEqualTo(DirtiesContext.ClassMode.AFTER_CLASS);
	}
}
