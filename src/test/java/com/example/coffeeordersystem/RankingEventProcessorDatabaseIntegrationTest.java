// 실제 MySQL에서 Consumer 처리 이력의 unique와 트랜잭션 rollback을 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.coffeeordersystem.event.domain.ProcessedEvent;
import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.ranking.consumer.RankingEventProcessor;
import com.example.coffeeordersystem.ranking.service.PopularMenuRankingService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RankingEventProcessorDatabaseIntegrationTest {

	@Autowired
	RankingEventProcessor processor;

	@Autowired
	ProcessedEventRepository processedEventRepository;

	@MockitoBean
	PopularMenuRankingService rankingService;

	@BeforeEach
	void setUp() {
		processedEventRepository.deleteAll();
		reset(rankingService);
	}

	@Test
	void commitsOneHistoryRowAndSkipsCompletedDuplicate() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);

		processor.process(event);
		processor.process(event);

		assertThat(processedEventRepository.findAll())
				.singleElement()
				.satisfies(processed -> {
					assertThat(processed.getEventId()).isEqualTo(event.eventId().toString());
					assertThat(processed.getEventType()).isEqualTo("order.completed");
					assertThat(processed.getConsumerGroup()).isEqualTo("ranking-consumer-group");
					assertThat(processed.getProcessedAt()).isNotNull();
				});
		verify(rankingService, times(1)).increment(event.eventId().toString(), event.menuId(), event.orderedAt());
	}

	@Test
	void commitsDifferentEventIdsIndependently() {
		OrderCompletedEvent first = event(UUID.randomUUID(), 11L);
		OrderCompletedEvent second = event(UUID.randomUUID(), 12L);

		processor.process(first);
		processor.process(second);

		assertThat(processedEventRepository.count()).isEqualTo(2);
		verify(rankingService).increment(first.eventId().toString(), first.menuId(), first.orderedAt());
		verify(rankingService).increment(second.eventId().toString(), second.menuId(), second.orderedAt());
	}

	@Test
	void rollsBackHistoryWhenRankingUpdateFails() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		doThrow(new IllegalStateException("redis unavailable"))
				.when(rankingService).increment(any(), any(), any());

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(IllegalStateException.class);

		assertThat(processedEventRepository.existsByEventId(event.eventId().toString())).isFalse();
	}

	private OrderCompletedEvent event(UUID eventId, Long menuId) {
		return new OrderCompletedEvent(
				eventId, 1L, 2L, menuId, 4_500, LocalDateTime.of(2026, 7, 11, 13, 0));
	}
}
