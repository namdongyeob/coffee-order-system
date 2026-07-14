package com.example.coffeeordersystem.order.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false, length = 100, unique = true)
	private String eventId;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	@Lob
	@Column(nullable = false)
	private String payload;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	protected OutboxEvent() {
	}

	public OutboxEvent(String eventId, String eventType, String payload, LocalDateTime createdAt) {
		this.eventId = eventId;
		this.eventType = eventType;
		this.payload = payload;
		this.createdAt = createdAt;
	}

	public void markPublished(LocalDateTime publishedAt) {
		this.publishedAt = publishedAt;
	}

	public Long getId() {
		return id;
	}

	public String getEventId() {
		return eventId;
	}

	public String getEventType() {
		return eventType;
	}

	public String getPayload() {
		return payload;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getPublishedAt() {
		return publishedAt;
	}
}
