// 모든 Spring test context가 공유하는 Kafka, MySQL, Redis Testcontainers 수명 경계를 관리합니다.
package com.example.coffeeordersystem;

import java.util.stream.Stream;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class SharedTestcontainers {

	private static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.9.1"));
	private static final MySQLContainer MYSQL = new MySQLContainer(DockerImageName.parse("mysql:8.4.5"));
	private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4.2"))
			.withExposedPorts(6379);

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
}
