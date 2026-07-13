// 성공한 랭킹 복구의 집계와 Kafka 종료 offset을 전달합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import java.util.Map;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public record RankingRebuildResult(
		Map<RankingRebuildCount, Long> counts,
		Map<TopicPartition, OffsetAndMetadata> endOffsets) {
}
