// 모든 Spring test context가 공유하는 Kafka, MySQL, Redis Testcontainers 수명 경계를 관리합니다.
package com.example.coffeeordersystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class SharedTestcontainers {

	private static final List<String> SHARED_KAFKA_TOPICS = List.of("order.completed", "order.completed.DLT");

	private static final KafkaContainer KAFKA = new SharedKafkaContainer(
			DockerImageName.parse("apache/kafka-native:3.9.1"));
	private static final MySQLContainer MYSQL = new SharedMySQLContainer(DockerImageName.parse("mysql:8.4.5"));
	private static final GenericContainer<?> REDIS = new SharedRedisContainer(DockerImageName.parse("redis:7.4.2"));

	private SharedTestcontainers() {
	}

	public static KafkaContainer kafka() {
		return KAFKA;
	}

	public static MySQLContainer mysql() {
		return MYSQL;
	}

	public static GenericContainer<?> redis() {
		return REDIS;
	}

	public static synchronized void start() {
		if (!KAFKA.isRunning() || !MYSQL.isRunning() || !REDIS.isRunning()) {
			Startables.deepStart(Stream.of(KAFKA, MYSQL, REDIS)).join();
		}
	}

	/**
	 * Kafka를 공유하는 통합 테스트가 시작되기 전에 이전 테스트의 레코드를 제거합니다.
	 */
	public static synchronized void clearKafkaTopics() {
		if (!KAFKA.isRunning()) {
			start();
		}
		clearSharedKafkaTopics();
	}

	private static synchronized void clearSharedKafkaTopics() {
		try (AdminClient admin = AdminClient.create(Map.of(
				AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers()))) {
			Set<String> existingTopics = admin.listTopics().names().get();
			List<String> topics = SHARED_KAFKA_TOPICS.stream()
					.filter(existingTopics::contains)
					.toList();
			if (topics.isEmpty()) {
				return;
			}

			Map<String, TopicDescription> descriptions = admin.describeTopics(topics).allTopicNames().get();
			Map<TopicPartition, OffsetSpec> endOffsets = new HashMap<>();
			for (TopicDescription description : descriptions.values()) {
				for (TopicPartitionInfo partition : description.partitions()) {
					endOffsets.put(new TopicPartition(description.name(), partition.partition()), OffsetSpec.latest());
				}
			}
			Map<TopicPartition, Long> offsets = admin.listOffsets(endOffsets).all().get().entrySet().stream()
					.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().offset()));
			Map<TopicPartition, RecordsToDelete> records = offsets.entrySet().stream()
					.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
							entry -> RecordsToDelete.beforeOffset(entry.getValue())));
			if (!records.isEmpty()) {
				admin.deleteRecords(records).all().get();
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("공유 Kafka topic 정리 중 인터럽트가 발생했습니다.", exception);
		} catch (Exception exception) {
			throw new IllegalStateException("공유 Kafka topic을 정리하지 못했습니다.", exception);
		}
	}

	private static final class SharedKafkaContainer extends KafkaContainer {

		private SharedKafkaContainer(DockerImageName image) {
			super(image);
		}

		@Override
		public void start() {
			super.start();
			clearSharedKafkaTopics();
		}

		@Override
		public void stop() {
			// Spring Boot's testcontainers lifecycle must not stop the shared static container.
		}
	}

	private static final class SharedMySQLContainer extends MySQLContainer {

		private SharedMySQLContainer(DockerImageName image) {
			super(image);
		}

		@Override
		public void stop() {
			// Spring Boot's testcontainers lifecycle must not stop the shared static container.
		}
	}

	private static final class SharedRedisContainer extends GenericContainer<SharedRedisContainer> {

		private SharedRedisContainer(DockerImageName image) {
			super(image);
			withExposedPorts(6379);
		}

		@Override
		public void stop() {
			// Spring Boot's testcontainers lifecycle must not stop the shared static container.
		}
	}
}
