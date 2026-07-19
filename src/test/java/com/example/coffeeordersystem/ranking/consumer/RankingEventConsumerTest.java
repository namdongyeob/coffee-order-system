// Kafka source header의 normal·DLT replay 신뢰 경계를 검증합니다.
package com.example.coffeeordersystem.ranking.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class RankingEventConsumerTest {

	@Mock RankingEventProcessor processor;
	RankingEventConsumer consumer;
	OrderCompletedEvent event;

	@BeforeEach
	void setUp() {
		consumer = new RankingEventConsumer(processor);
		event = new OrderCompletedEvent(
				UUID.randomUUID(), 1L, 2L, 3L, 4_500, LocalDateTime.of(2026, 7, 13, 10, 0));
	}

	@Test
	void missingReplayHeaderUsesNormalConsumerSource() {
		consumer.consume(event, null, 1, 10L);

		verify(processor).process(event);
	}

	@Test
	void dltReplayHeaderUsesDltSource() {
		consumer.consume(event, RankingReplayHeaders.DLT_REPLAY.getBytes(StandardCharsets.UTF_8), 1, 10L);

		verify(processor).process(event, RankingEventSource.DLT_REPLAY);
	}

	@Test
	void unknownReplayHeaderFailsClosedBeforeProcessor() {
		assertThatThrownBy(() -> consumer.consume(event, "UNTRUSTED".getBytes(StandardCharsets.UTF_8), 1, 10L))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("지원하지 않는");

		verify(processor, never()).process(event);
		verify(processor, never()).process(event, RankingEventSource.DLT_REPLAY);
	}

	@Test
	void rebuildFenceLogIncludesSharedOwnerAndRun(CapturedOutput output) {
		String lockOwner = "owner=REBUILD,runId=" + UUID.randomUUID();
		doThrow(new RankingRebuildInProgressException(event.eventId().toString(), lockOwner))
				.when(processor).process(event);

		assertThatThrownBy(() -> consumer.consume(event, null, 1, 10L))
				.isInstanceOf(RankingRebuildInProgressException.class);

		assertThat(output.getOut())
				.contains("ranking_consumer_rebuild_fenced")
				.contains("eventId=" + event.eventId())
				.contains("partition=1 offset=10")
				.contains("lockOwner=" + lockOwner);
	}
}
