// 주문 완료 Consumer 실패를 제한 재시도한 뒤 DLT로 격리합니다.
package com.example.coffeeordersystem.ranking.consumer;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.BackOffHandler;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerPausingBackOffHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultBackOffHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ErrorHandlingUtils;
import org.springframework.kafka.listener.ListenerContainerPauseService;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
public class KafkaConsumerErrorHandlerConfiguration {

	static final long RETRY_INTERVAL_MILLIS = 1_000L;
	static final long RETRY_ATTEMPTS = 2L;

	@Bean
	CommonErrorHandler kafkaConsumerErrorHandler(
			KafkaTemplate<Object, Object> kafkaTemplate,
			KafkaListenerEndpointRegistry listenerRegistry,
			TaskScheduler taskScheduler) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				kafkaTemplate,
				(record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
		DefaultBackOffHandler defaultBackOffHandler = new DefaultBackOffHandler();
		ContainerPausingBackOffHandler pausingBackOffHandler = new ContainerPausingBackOffHandler(
				new ListenerContainerPauseService(listenerRegistry, taskScheduler));
		BackOffHandler selectiveBackOffHandler = new BackOffHandler() {
			@Override
			public void onNextBackOff(
					MessageListenerContainer container,
					Exception exception,
					long nextBackOff) {
				if (ErrorHandlingUtils.findRootCause(exception) instanceof RankingRebuildInProgressException) {
					pausingBackOffHandler.onNextBackOff(container, exception, nextBackOff);
					return;
				}
				defaultBackOffHandler.onNextBackOff(container, exception, nextBackOff);
			}
		};
		DefaultErrorHandler errorHandler = new DefaultErrorHandler(
				recoverer,
				new FixedBackOff(RETRY_INTERVAL_MILLIS, RETRY_ATTEMPTS),
				selectiveBackOffHandler);
		errorHandler.setBackOffFunction((record, exception) ->
				exception instanceof RankingRebuildInProgressException
						? new FixedBackOff(RETRY_INTERVAL_MILLIS, FixedBackOff.UNLIMITED_ATTEMPTS)
						: null);
		return errorHandler;
	}
}
