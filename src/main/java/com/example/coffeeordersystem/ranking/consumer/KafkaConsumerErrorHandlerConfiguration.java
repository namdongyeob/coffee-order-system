// 주문 완료 Consumer 실패를 제한 재시도한 뒤 DLT로 격리합니다.
package com.example.coffeeordersystem.ranking.consumer;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
public class KafkaConsumerErrorHandlerConfiguration {

	static final long RETRY_INTERVAL_MILLIS = 1_000L;
	static final long RETRY_ATTEMPTS = 2L;

	@Bean
	CommonErrorHandler kafkaConsumerErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				kafkaTemplate,
				(record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
		return new DefaultErrorHandler(
				recoverer,
				new FixedBackOff(RETRY_INTERVAL_MILLIS, RETRY_ATTEMPTS));
	}
}
