// 정상 consumer group offset을 이동하고 실패 시 이전 상태로 검증 복원합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.springframework.stereotype.Component;

@Component
class RankingRebuildOffsetManager {

	private static final String GROUP = "ranking-consumer-group";

	OffsetSnapshot capture(AdminClient admin, Collection<TopicPartition> partitions) throws Exception {
		Map<TopicPartition, OffsetAndMetadata> committed = readCommitted(admin);
		Map<TopicPartition, Optional<OffsetAndMetadata>> values = new HashMap<>();
		partitions.forEach(partition -> values.put(partition, Optional.ofNullable(committed.get(partition))));
		return new OffsetSnapshot(Map.copyOf(values));
	}

	void move(AdminClient admin, Map<TopicPartition, OffsetAndMetadata> target) throws Exception {
		admin.alterConsumerGroupOffsets(GROUP, target).all().get(10, TimeUnit.SECONDS);
	}

	void verify(AdminClient admin, Map<TopicPartition, OffsetAndMetadata> expected) throws Exception {
		Map<TopicPartition, OffsetAndMetadata> actual = readCommitted(admin);
		for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : expected.entrySet()) {
			OffsetAndMetadata actualOffset = actual.get(entry.getKey());
			if (actualOffset == null || actualOffset.offset() != entry.getValue().offset()) {
				throw new RankingRebuildException("normal consumer offset 이동 확인에 실패했습니다");
			}
		}
	}

	void restoreAndVerify(AdminClient admin, OffsetSnapshot snapshot) throws Exception {
		Map<TopicPartition, OffsetAndMetadata> existing = snapshot.values().entrySet().stream()
				.filter(entry -> entry.getValue().isPresent())
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().orElseThrow()));
		Set<TopicPartition> absent = snapshot.values().entrySet().stream()
				.filter(entry -> entry.getValue().isEmpty())
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		if (!existing.isEmpty()) {
			admin.alterConsumerGroupOffsets(GROUP, existing).all().get(10, TimeUnit.SECONDS);
		}
		if (!absent.isEmpty()) {
			admin.deleteConsumerGroupOffsets(GROUP, absent).all().get(10, TimeUnit.SECONDS);
		}
		Map<TopicPartition, OffsetAndMetadata> actual = readCommitted(admin);
		for (Map.Entry<TopicPartition, Optional<OffsetAndMetadata>> entry : snapshot.values().entrySet()) {
			OffsetAndMetadata current = actual.get(entry.getKey());
			if (entry.getValue().isEmpty() ? current != null : current == null
					|| (current != null && current.offset() != entry.getValue().orElseThrow().offset())) {
				throw new RankingRebuildException("normal consumer offset 복원 확인에 실패했습니다");
			}
		}
	}

	private Map<TopicPartition, OffsetAndMetadata> readCommitted(AdminClient admin) throws Exception {
		try {
			return admin.listConsumerGroupOffsets(GROUP).partitionsToOffsetAndMetadata().get(10, TimeUnit.SECONDS);
		} catch (ExecutionException exception) {
			if (exception.getCause() instanceof GroupIdNotFoundException) {
				return Map.of();
			}
			throw exception;
		}
	}

	record OffsetSnapshot(Map<TopicPartition, Optional<OffsetAndMetadata>> values) {
	}
}
