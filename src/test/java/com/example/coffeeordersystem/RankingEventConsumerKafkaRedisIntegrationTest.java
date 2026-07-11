// 실제 Kafka listener가 MySQL 멱등 이력과 Redis 랭킹을 함께 갱신하는지 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RankingEventConsumerKafkaRedisIntegrationTest {

	@Autowired
	OrderEventPublisher publisher;

	@Autowired
	ProcessedEventRepository processedEventRepository;

	@Autowired
	StringRedisTemplate redisTemplate;

	@BeforeEach
	void setUp() {
		processedEventRepository.deleteAll();
		redisTemplate.delete(redisTemplate.keys("popular:menus:*"));
	}

	@Test
	void consumesNewEventAndSkipsCompletedDuplicate() throws Exception {
		OrderCompletedEvent event = new OrderCompletedEvent(
				UUID.randomUUID(), 1L, 2L, 11L, 4_500, LocalDateTime.of(2026, 7, 11, 13, 0));

		publisher.publish(event).get();
		await(Duration.ofSeconds(10), () -> processedEventRepository.existsByEventId(event.eventId().toString()));
		publisher.publish(event).get();
		await(Duration.ofSeconds(10), () -> Double.valueOf(1.0).equals(score(event)));
		Thread.sleep(1_000);

		assertThat(processedEventRepository.count()).isEqualTo(1);
		assertThat(score(event)).isEqualTo(1.0);
	}

	private Double score(OrderCompletedEvent event) {
		return redisTemplate.opsForZSet().score("popular:menus:2026-07-11", event.menuId().toString());
	}

	private void await(Duration timeout, BooleanSupplier condition) throws Exception {
		long deadline = System.nanoTime() + timeout.toNanos();
		while (System.nanoTime() < deadline) {
			if (condition.getAsBoolean()) {
				return;
			}
			Thread.sleep(100);
		}
		assertThat(condition.getAsBoolean()).isTrue();
	}
}
