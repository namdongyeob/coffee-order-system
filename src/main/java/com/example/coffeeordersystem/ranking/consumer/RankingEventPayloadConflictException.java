// 동일 eventId의 core payload가 다를 때 ranking 변경을 fail-closed로 차단합니다.
package com.example.coffeeordersystem.ranking.consumer;

public class RankingEventPayloadConflictException extends IllegalStateException {

	public RankingEventPayloadConflictException(String eventId) {
		super("EVENT_ID_PAYLOAD_CONFLICT eventId=" + eventId);
	}
}
