// Kafka 발행이 계속 실패해도 주문 DB 커밋과 OutboxEvent 저장이 영향받지 않음을 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.coffeeordersystem.order.dto.OrderResponse;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.order.event.OutboxEvent;
import com.example.coffeeordersystem.order.event.OutboxEventPublisher;
import com.example.coffeeordersystem.order.event.OutboxEventRepository;
import com.example.coffeeordersystem.order.service.OrderService;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "ranking.consumer.enabled=false")
class OutboxEventKafkaUnavailableIntegrationTest {

	@Autowired
	OrderService orderService;

	@Autowired
	UserPointRepository userPointRepository;

	@Autowired
	OutboxEventRepository outboxEventRepository;

	@Autowired
	OutboxEventPublisher outboxEventPublisher;

	@MockitoBean
	OrderEventPublisher orderEventPublisher;

	@BeforeEach
	void setUp() {
		outboxEventRepository.deleteAll();
		when(orderEventPublisher.publish(any()))
				.thenReturn(CompletableFuture.failedFuture(new IllegalStateException("kafka unavailable")));
	}

	@Test
	void createOrderCommitsAndKeepsOutboxEventPendingWhenKafkaAlwaysFails() {
		userPointRepository.save(new UserPoint(301L, 10_000));

		OrderResponse response = orderService.createOrder(301L, 1L);

		assertThat(response.status().name()).isEqualTo("PAID");
		assertThat(userPointRepository.findByUserId(301L)).get()
				.extracting(UserPoint::getBalance)
				.isEqualTo(5_500);
		OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
		assertThat(outboxEvent.getPublishedAt()).isNull();

		outboxEventPublisher.publishPending();

		assertThat(outboxEventRepository.findById(outboxEvent.getId()))
				.get()
				.extracting(OutboxEvent::getPublishedAt)
				.isNull();
	}
}
