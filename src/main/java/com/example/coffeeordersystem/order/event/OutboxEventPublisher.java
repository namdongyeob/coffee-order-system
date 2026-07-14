// 미발행 OutboxEvent를 주기적으로 조회해 Kafka로 발행하고 발행 상태를 갱신한다.
package com.example.coffeeordersystem.order.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

	private static final int PUBLISH_TIMEOUT_SECONDS = 5;

	private final OutboxEventRepository outboxEventRepository;
	private final OrderEventPublisher orderEventPublisher;
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@Scheduled(fixedDelayString = "${outbox.publish.fixed-delay-ms:2000}")
	public void publishPending() {
		List<OutboxEvent> pending = outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc();
		for (OutboxEvent outboxEvent : pending) {
			publishOne(outboxEvent);
		}
	}

	private void publishOne(OutboxEvent outboxEvent) {
		OrderCompletedEvent event;
		try {
			event = objectMapper.readValue(outboxEvent.getPayload(), OrderCompletedEvent.class);
		} catch (JsonProcessingException exception) {
			log.error("outbox_event_payload_deserialize_failed id={} eventId={}",
					outboxEvent.getId(), outboxEvent.getEventId(), exception);
			return;
		}
		try {
			orderEventPublisher.publish(event).get(PUBLISH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			outboxEvent.markPublished(LocalDateTime.now());
			outboxEventRepository.save(outboxEvent);
		} catch (Exception exception) {
			log.error("outbox_event_publish_failed id={} eventId={}",
					outboxEvent.getId(), outboxEvent.getEventId(), exception);
		}
	}
}
