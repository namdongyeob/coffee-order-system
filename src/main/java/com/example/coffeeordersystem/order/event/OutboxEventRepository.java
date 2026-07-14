package com.example.coffeeordersystem.order.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

	List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByIdAsc();

	boolean existsByEventId(String eventId);
}
