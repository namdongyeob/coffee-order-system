package com.example.coffeeordersystem.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false, length = 100, unique = true)
	private String eventId;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	@Column(name = "consumer_group", nullable = false, length = 100)
	private String consumerGroup;

	@Column(name = "processed_at", nullable = false)
	private LocalDateTime processedAt;

	public ProcessedEvent(String eventId, String eventType, String consumerGroup, LocalDateTime processedAt) {
		this.eventId = eventId;
		this.eventType = eventType;
		this.consumerGroup = consumerGroup;
		this.processedAt = processedAt;
	}
}
