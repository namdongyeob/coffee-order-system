// rebuild recovery fence가 해제될 때까지 Kafka record 재시도를 요청합니다.
package com.example.coffeeordersystem.ranking.consumer;

public class RankingRebuildInProgressException extends RuntimeException {

	public RankingRebuildInProgressException(String eventId) {
		super("RANKING_REBUILD_FENCE_BUSY eventId=" + eventId);
	}
}
