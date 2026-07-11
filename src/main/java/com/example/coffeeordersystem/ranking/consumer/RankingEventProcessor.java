// 주문 완료 이벤트의 처리 이력과 Redis 랭킹 갱신을 한 트랜잭션 경계에서 조정합니다.
package com.example.coffeeordersystem.ranking.consumer;

import com.example.coffeeordersystem.event.domain.ProcessedEvent;
import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.ranking.service.PopularMenuRankingService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingEventProcessor {

	static final String EVENT_TYPE = "order.completed";
	static final String CONSUMER_GROUP = "ranking-consumer-group";

	private final ProcessedEventRepository processedEventRepository;
	private final PopularMenuRankingService rankingService;

	@Transactional
	public void process(OrderCompletedEvent event) {
		String eventId = event.eventId().toString();
		if (processedEventRepository.existsByEventId(eventId)) {
			return;
		}

		processedEventRepository.saveAndFlush(new ProcessedEvent(
				eventId, EVENT_TYPE, CONSUMER_GROUP, LocalDateTime.now()));
		rankingService.increment(event.menuId(), event.orderedAt());
	}
}
