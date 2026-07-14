// 주문 완료 이벤트의 멱등 처리와 랭킹 호출 경계를 검증합니다.
package com.example.coffeeordersystem.ranking.consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.coffeeordersystem.event.domain.ProcessedEvent;
import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.ranking.service.PopularMenuRankingService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankingEventProcessorTest {

	@Mock
	ProcessedEventRepository processedEventRepository;

	@Mock
	PopularMenuRankingService rankingService;

	RankingEventProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new RankingEventProcessor(processedEventRepository, rankingService);
	}

	@Test
	void savesAndFlushesNewEventBeforeIncrementingRanking() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		when(processedEventRepository.existsByEventId(event.eventId().toString())).thenReturn(false);

		processor.process(event);

		InOrder inOrder = inOrder(processedEventRepository, rankingService);
		inOrder.verify(processedEventRepository).saveAndFlush(any(ProcessedEvent.class));
		inOrder.verify(rankingService).increment(event.eventId().toString(), event.menuId(), event.orderedAt());
	}

	@Test
	void skipsAlreadyProcessedEvent() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		when(processedEventRepository.existsByEventId(event.eventId().toString())).thenReturn(true);

		processor.process(event);

		verify(processedEventRepository, never()).saveAndFlush(any(ProcessedEvent.class));
		verify(rankingService, never()).increment(any(), any(), any());
	}

	@Test
	void processesDifferentEventIdsIndependently() {
		OrderCompletedEvent first = event(UUID.randomUUID(), 11L);
		OrderCompletedEvent second = event(UUID.randomUUID(), 12L);
		when(processedEventRepository.existsByEventId(any())).thenReturn(false);

		processor.process(first);
		processor.process(second);

		verify(processedEventRepository, org.mockito.Mockito.times(2)).saveAndFlush(any(ProcessedEvent.class));
		verify(rankingService).increment(first.eventId().toString(), first.menuId(), first.orderedAt());
		verify(rankingService).increment(second.eventId().toString(), second.menuId(), second.orderedAt());
	}

	@Test
	void propagatesRankingFailure() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		when(processedEventRepository.existsByEventId(event.eventId().toString())).thenReturn(false);
		doThrow(new IllegalStateException("redis unavailable"))
				.when(rankingService).increment(event.eventId().toString(), event.menuId(), event.orderedAt());

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("redis unavailable");
	}

	private OrderCompletedEvent event(UUID eventId, Long menuId) {
		return new OrderCompletedEvent(
				eventId, 1L, 2L, menuId, 4_500, LocalDateTime.of(2026, 7, 11, 13, 0));
	}
}
