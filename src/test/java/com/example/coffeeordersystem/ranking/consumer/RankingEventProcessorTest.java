// 주문 완료 이벤트의 멱등 처리와 랭킹 호출 경계를 검증합니다.
package com.example.coffeeordersystem.ranking.consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.coffeeordersystem.event.domain.ProcessedEvent;
import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.ranking.rebuild.RankingRebuildLock;
import com.example.coffeeordersystem.ranking.service.PopularMenuRankingService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RankingEventProcessorTest {

	@Mock
	ProcessedEventRepository processedEventRepository;

	@Mock
	PopularMenuRankingService rankingService;

	@Mock
	RankingEventLedger rankingEventLedger;

	@Mock
	RankingRebuildLock recoveryLock;

	@Mock
	StringRedisTemplate redis;

	@Mock
	ValueOperations<String, String> values;

	RankingEventProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new RankingEventProcessor(
				processedEventRepository, rankingService, rankingEventLedger, recoveryLock, redis);
		when(recoveryLock.acquire(any())).thenReturn(true);
	}

	@Test
	void appliesLedgerAndRankingBeforeSavingCompatibilityHistory() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		String fingerprint = RankingEventFingerprint.from(event);
		when(rankingEventLedger.reserve(
				event.eventId().toString(), fingerprint, RankingEventSource.NORMAL_CONSUMER))
				.thenReturn(new RankingEventLedger.Reservation(false, false));
		when(processedEventRepository.existsByEventId(event.eventId().toString())).thenReturn(false);

		processor.process(event);

		InOrder inOrder = inOrder(rankingEventLedger, rankingService, processedEventRepository);
		inOrder.verify(rankingEventLedger).reserve(
				event.eventId().toString(), fingerprint, RankingEventSource.NORMAL_CONSUMER);
		inOrder.verify(rankingService).apply(event.eventId().toString(), fingerprint, event.menuId(), event.orderedAt());
		inOrder.verify(rankingEventLedger).markRedisApplied(event.eventId().toString());
		inOrder.verify(rankingEventLedger).markCommitted(event.eventId().toString());
		inOrder.verify(processedEventRepository).saveAndFlush(any(ProcessedEvent.class));
	}

	@Test
	void rebuildFenceBusyStopsBeforeLedgerAndRedisMutation() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		String lockOwner = "owner=REBUILD,runId=" + UUID.randomUUID();
		when(recoveryLock.acquire(any())).thenReturn(false);
		when(redis.opsForValue()).thenReturn(values);
		when(values.get("ranking:rebuild:lock")).thenReturn(lockOwner);

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(RankingRebuildInProgressException.class)
				.hasMessageContaining(event.eventId().toString())
				.hasMessageContaining(lockOwner);

		verifyNoInteractions(rankingEventLedger, rankingService, processedEventRepository);
		verify(recoveryLock, never()).release(any());
	}

	@Test
	void skipsAlreadyProcessedEvent() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		when(rankingEventLedger.reserve(any(), any(), any()))
				.thenReturn(new RankingEventLedger.Reservation(true, false));
		when(processedEventRepository.existsByEventId(event.eventId().toString())).thenReturn(true);

		processor.process(event);

		verify(processedEventRepository, never()).saveAndFlush(any(ProcessedEvent.class));
		verify(rankingService, never()).apply(any(), any(), any(), any());
	}

	@Test
	void processesDifferentEventIdsIndependently() {
		OrderCompletedEvent first = event(UUID.randomUUID(), 11L);
		OrderCompletedEvent second = event(UUID.randomUUID(), 12L);
		when(rankingEventLedger.reserve(any(), any(), any()))
				.thenReturn(new RankingEventLedger.Reservation(false, false));
		when(processedEventRepository.existsByEventId(any())).thenReturn(false);

		processor.process(first);
		processor.process(second);

		verify(processedEventRepository, org.mockito.Mockito.times(2)).saveAndFlush(any(ProcessedEvent.class));
		verify(rankingService).apply(
				first.eventId().toString(), RankingEventFingerprint.from(first), first.menuId(), first.orderedAt());
		verify(rankingService).apply(
				second.eventId().toString(), RankingEventFingerprint.from(second), second.menuId(), second.orderedAt());
	}

	@Test
	void propagatesRankingFailure() {
		OrderCompletedEvent event = event(UUID.randomUUID(), 11L);
		when(rankingEventLedger.reserve(any(), any(), any()))
				.thenReturn(new RankingEventLedger.Reservation(false, false));
		doThrow(new IllegalStateException("redis unavailable"))
				.when(rankingService).apply(
						event.eventId().toString(), RankingEventFingerprint.from(event), event.menuId(), event.orderedAt());

		assertThatThrownBy(() -> processor.process(event))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("redis unavailable");
	}

	private OrderCompletedEvent event(UUID eventId, Long menuId) {
		return new OrderCompletedEvent(
				eventId, 1L, 2L, menuId, 4_500, LocalDateTime.of(2026, 7, 11, 13, 0));
	}
}
