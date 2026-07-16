// 검증·예약이 끝난 DLT record를 내부 source header와 함께 원본 topic으로 발행합니다.
package com.example.coffeeordersystem.recovery;

import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.ranking.consumer.RankingReplayHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.stereotype.Component;

@Component
public class DltReplayPublisher {

	private final String bootstrapServers;

	public DltReplayPublisher(KafkaConnectionDetails connectionDetails) {
		this.bootstrapServers = String.join(",", connectionDetails.getBootstrapServers());
	}

	public void publish(ConsumerRecord<String, String> record) {
		try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties())) {
			ProducerRecord<String, String> replayed = new ProducerRecord<>(
					OrderEventPublisher.ORDER_COMPLETED_TOPIC, record.key(), record.value());
			replayed.headers().add("__TypeId__", OrderCompletedEvent.class.getName().getBytes(StandardCharsets.UTF_8));
			replayed.headers().add(RankingReplayHeaders.SOURCE,
					RankingReplayHeaders.DLT_REPLAY.getBytes(StandardCharsets.UTF_8));
			producer.send(replayed).get(10, TimeUnit.SECONDS);
		} catch (DltReplayException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new DltReplayException("원본 topic으로 재발행하지 못했습니다.", exception);
		}
	}

	private Map<String, Object> producerProperties() {
		return Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.ACKS_CONFIG, "all");
	}
}
