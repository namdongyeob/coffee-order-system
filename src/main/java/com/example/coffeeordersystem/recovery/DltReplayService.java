// 승인된 DLT 한 건을 원본 topic으로 안전하게 재발행합니다.
package com.example.coffeeordersystem.recovery;

import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

@Service
public class DltReplayService {

	static final String DLT_TOPIC = OrderEventPublisher.ORDER_COMPLETED_TOPIC + ".DLT";
	private static final Duration POLL_TIMEOUT = Duration.ofMillis(250);
	private static final Duration RECORD_TIMEOUT = Duration.ofSeconds(10);
	private static final String RACE_RISK = "processed_event 사전 조회 뒤 consumer 처리와 경쟁할 수 있으며 최종 중복 방어는 consumer 멱등성에 맡깁니다.";

	private final ProcessedEventRepository processedEvents;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String bootstrapServers;

	public DltReplayService(
			ProcessedEventRepository processedEvents,
			KafkaConnectionDetails connectionDetails) {
		this.processedEvents = processedEvents;
		this.bootstrapServers = String.join(",", connectionDetails.getBootstrapServers());
	}

	public DltReplayResult replay(DltReplayRequest request) {
		validate(request);
		ConsumerRecord<String, String> record = loadOne(request);
		verifyOriginalHeaders(record);
		String eventId = eventId(record.value());
		if (processedEvents.existsByEventId(eventId)) {
			return new DltReplayResult(DltReplayStatus.SKIPPED_ALREADY_PROCESSED, eventId, RACE_RISK);
		}
		publishOriginal(record);
		return new DltReplayResult(DltReplayStatus.REPUBLISHED, eventId, RACE_RISK);
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

	private String eventId(String payload) {
		try {
			JsonNode eventId = objectMapper.readTree(payload).path("eventId");
			if (!eventId.isTextual()) {
				throw new DltReplayException("payload에 eventId가 없습니다.");
			}
			return UUID.fromString(eventId.asText()).toString();
		} catch (DltReplayException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new DltReplayException("payload eventId를 검증하지 못했습니다.", exception);
		}
	}

	private void publishOriginal(ConsumerRecord<String, String> record) {
		try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties())) {
			ProducerRecord<String, String> replayed = new ProducerRecord<>(
					OrderEventPublisher.ORDER_COMPLETED_TOPIC, record.key(), record.value());
			replayed.headers().add("__TypeId__", OrderCompletedEvent.class.getName().getBytes(StandardCharsets.UTF_8));
			producer.send(replayed)
					.get(10, TimeUnit.SECONDS);
		} catch (Exception exception) {
			throw new DltReplayException("원본 topic으로 재발행하지 못했습니다.", exception);
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

	private Map<String, Object> producerProperties() {
		return Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.ACKS_CONFIG, "all");
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
