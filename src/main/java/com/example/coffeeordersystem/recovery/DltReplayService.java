// 승인된 DLT 한 건을 원본 topic으로 안전하게 재발행합니다.
package com.example.coffeeordersystem.recovery;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.ranking.consumer.RankingEventFingerprint;
import com.example.coffeeordersystem.ranking.consumer.RankingEventLedger;
import com.example.coffeeordersystem.ranking.rebuild.RankingRebuildLock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DltReplayService {

	static final String DLT_TOPIC = OrderEventPublisher.ORDER_COMPLETED_TOPIC + ".DLT";
	private static final Duration POLL_TIMEOUT = Duration.ofMillis(250);
	private static final Duration RECORD_TIMEOUT = Duration.ofSeconds(10);
	private static final String RECOVERY_GUARANTEE = "공통 ranking ledger와 recovery lock이 DLT replay와 rebuild의 중복 집계를 방지합니다.";

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	private final String bootstrapServers;
	private final RankingRebuildLock recoveryLock;
	private final JdbcTemplate jdbc;
	private final RankingEventLedger rankingEventLedger;
	private final DltReplayPublisher replayPublisher;

	public DltReplayService(
			KafkaConnectionDetails connectionDetails,
			RankingRebuildLock recoveryLock,
			JdbcTemplate jdbc,
			RankingEventLedger rankingEventLedger,
			DltReplayPublisher replayPublisher) {
		this.bootstrapServers = String.join(",", connectionDetails.getBootstrapServers());
		this.recoveryLock = recoveryLock;
		this.jdbc = jdbc;
		this.rankingEventLedger = rankingEventLedger;
		this.replayPublisher = replayPublisher;
	}

	public DltReplayResult replay(DltReplayRequest request) {
		validate(request);
		String token = UUID.randomUUID().toString();
		if (!recoveryLock.acquire(token)) {
			throw new DltReplayRetryableException("ranking recovery lock이 사용 중이므로 DLT 재발행을 재시도해야 합니다.");
		}
		try {
			if (hasPendingRebuild()) {
				throw new DltReplayRetryableException("pending rebuild가 있어 DLT 재발행을 재시도해야 합니다.");
			}
			ConsumerRecord<String, String> record = loadOne(request);
			verifyOriginalHeaders(record);
			OrderCompletedEvent event = event(record.value());
			String eventId = event.eventId().toString();
			rankingEventLedger.reserveReplay(eventId, RankingEventFingerprint.from(event));
			if (!recoveryLock.renew(token)) {
				throw new DltReplayRetryableException("ranking recovery lock 소유권을 잃어 DLT 재발행을 재시도해야 합니다.");
			}
			replayPublisher.publish(record);
			return new DltReplayResult(DltReplayStatus.REPUBLISHED, eventId, RECOVERY_GUARANTEE);
		} finally {
			recoveryLock.release(token);
		}
	}

	private boolean hasPendingRebuild() {
		Long count = jdbc.queryForObject(
				"select count(*) from ranking_rebuild_run where state <> 'COMPLETED'", Long.class);
		return count != null && count > 0;
	}

	private void validate(DltReplayRequest request) {
		if (request == null || !DLT_TOPIC.equals(request.dltTopic())) {
			throw new DltReplayException("order.completed.DLT의 한 레코드만 재발행할 수 있습니다.");
		}
		if (request.partition() < 0 || request.offset() < 0) {
			throw new DltReplayException("partition과 offset은 0 이상이어야 합니다.");
		}
		if (isBlank(request.approvedBy()) || isBlank(request.reason())) {
			throw new DltReplayException("승인자와 재처리 사유는 필수입니다.");
		}
	}

	private ConsumerRecord<String, String> loadOne(DltReplayRequest request) {
		TopicPartition partition = new TopicPartition(request.dltTopic(), request.partition());
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties())) {
			consumer.assign(java.util.List.of(partition));
			consumer.seek(partition, request.offset());
			long deadline = System.nanoTime() + RECORD_TIMEOUT.toNanos();
			while (System.nanoTime() < deadline) {
				for (ConsumerRecord<String, String> record : consumer.poll(POLL_TIMEOUT)) {
					if (record.offset() == request.offset()) {
						return record;
					}
					if (record.offset() > request.offset()) {
						throw new DltReplayException("지정한 DLT offset의 메시지를 찾지 못했습니다.");
					}
				}
			}
			throw new DltReplayException("지정한 DLT offset의 메시지를 제한 시간 안에 찾지 못했습니다.");
		} catch (DltReplayException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new DltReplayException("DLT 메시지를 안전하게 재조회하지 못했습니다.", exception);
		}
	}

	private void verifyOriginalHeaders(ConsumerRecord<String, String> record) {
		String originalTopic = headerValue(record, KafkaHeaders.DLT_ORIGINAL_TOPIC, "원본 topic");
		requireHeader(record, KafkaHeaders.DLT_ORIGINAL_PARTITION, "원본 partition");
		requireHeader(record, KafkaHeaders.DLT_ORIGINAL_OFFSET, "원본 offset");
		if (!OrderEventPublisher.ORDER_COMPLETED_TOPIC.equals(originalTopic)) {
			throw new DltReplayException("DLT original topic이 order.completed와 일치하지 않습니다.");
		}
	}

	private OrderCompletedEvent event(String payload) {
		try {
			JsonNode eventId = objectMapper.readTree(payload).path("eventId");
			if (!eventId.isTextual()) {
				throw new DltReplayException("payload에 eventId가 없습니다.");
			}
			UUID.fromString(eventId.asText());
			return objectMapper.readValue(payload, OrderCompletedEvent.class);
		} catch (DltReplayException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new DltReplayException("payload eventId를 검증하지 못했습니다.", exception);
		}
	}

	private Header requireHeader(ConsumerRecord<String, String> record, String name, String description) {
		Header header = record.headers().lastHeader(name);
		if (header == null || header.value() == null || header.value().length == 0) {
			throw new DltReplayException("DLT " + description + " header가 없습니다.");
		}
		return header;
	}

	private String headerValue(ConsumerRecord<String, String> record, String name, String description) {
		return new String(requireHeader(record, name, description).value(), StandardCharsets.UTF_8);
	}

	private Map<String, Object> consumerProperties() {
		return Map.of(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
				ConsumerConfig.GROUP_ID_CONFIG, "dlt-replay-read-" + UUID.randomUUID(),
				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
				ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none");
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
