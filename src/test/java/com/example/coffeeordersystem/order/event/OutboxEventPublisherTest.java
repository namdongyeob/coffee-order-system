// OutboxEvent 발행 성공·실패·역직렬화 실패 시 마킹 경계를 검증합니다.
package com.example.coffeeordersystem.order.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

	@Mock
	OutboxEventRepository outboxEventRepository;

	@Mock
	OrderEventPublisher orderEventPublisher;

	final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	OutboxEventPublisher publisher;

	@BeforeEach
	void setUp() {
		publisher = new OutboxEventPublisher(outboxEventRepository, orderEventPublisher);
	}

	@Test
	void publishesPendingEventAndMarksItPublished() throws Exception {
		OutboxEvent outboxEvent = outboxEvent(UUID.randomUUID());
		when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc()).thenReturn(List.of(outboxEvent));
		when(orderEventPublisher.publish(any())).thenReturn(CompletableFuture.completedFuture(null));

		publisher.publishPending();

		verify(orderEventPublisher).publish(any(OrderCompletedEvent.class));
		assertThat(outboxEvent.getPublishedAt()).isNotNull();
		verify(outboxEventRepository).save(outboxEvent);
	}

	@Test
	void leavesEventPendingWhenPublishFails() {
		OutboxEvent outboxEvent = outboxEvent(UUID.randomUUID());
		when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc()).thenReturn(List.of(outboxEvent));
		when(orderEventPublisher.publish(any()))
				.thenReturn(CompletableFuture.failedFuture(new IllegalStateException("kafka unavailable")));

		publisher.publishPending();

		assertThat(outboxEvent.getPublishedAt()).isNull();
		verify(outboxEventRepository, never()).save(any());
	}

	@Test
	void skipsUnparseablePayloadWithoutStoppingBatch() {
		OutboxEvent broken = new OutboxEvent(
				UUID.randomUUID().toString(), OrderEventPublisher.ORDER_COMPLETED_TOPIC, "not-json", LocalDateTime.now());
		OutboxEvent healthy = outboxEvent(UUID.randomUUID());
		when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc()).thenReturn(List.of(broken, healthy));
		when(orderEventPublisher.publish(any())).thenReturn(CompletableFuture.completedFuture(null));

		publisher.publishPending();

		assertThat(broken.getPublishedAt()).isNull();
		assertThat(healthy.getPublishedAt()).isNotNull();
		verify(orderEventPublisher).publish(any(OrderCompletedEvent.class));
		verify(outboxEventRepository, never()).save(broken);
		verify(outboxEventRepository).save(healthy);
	}

	@Test
	void publishesOnSubsequentPollAfterTransientFailure() {
		OutboxEvent outboxEvent = outboxEvent(UUID.randomUUID());
		when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc()).thenReturn(List.of(outboxEvent));
		when(orderEventPublisher.publish(any()))
				.thenReturn(CompletableFuture.failedFuture(new IllegalStateException("kafka unavailable")))
				.thenReturn(CompletableFuture.completedFuture(null));

		publisher.publishPending();
		assertThat(outboxEvent.getPublishedAt()).isNull();

		publisher.publishPending();
		assertThat(outboxEvent.getPublishedAt()).isNotNull();
	}

	private OutboxEvent outboxEvent(UUID eventId) {
		OrderCompletedEvent event = new OrderCompletedEvent(
				eventId, 1L, 2L, 3L, 4_500, LocalDateTime.of(2026, 7, 11, 13, 0));
		try {
			return new OutboxEvent(
					eventId.toString(), OrderEventPublisher.ORDER_COMPLETED_TOPIC,
					objectMapper.writeValueAsString(event), LocalDateTime.now());
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
