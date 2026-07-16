// DLT replay가 normal consumer에 신뢰 가능한 내부 처리 경로를 전달하는 Kafka header를 정의합니다.
package com.example.coffeeordersystem.ranking.consumer;

public final class RankingReplayHeaders {

	public static final String SOURCE = "ranking-replay-source";
	public static final String DLT_REPLAY = "DLT_REPLAY";

	private RankingReplayHeaders() {
	}
}
