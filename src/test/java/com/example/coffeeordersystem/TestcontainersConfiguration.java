package com.example.coffeeordersystem;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
@Import(DisabledTaskSchedulerConfiguration.class)
public class TestcontainersConfiguration {

	@Bean(destroyMethod = "")
	@ServiceConnection
	KafkaContainer kafkaContainer() {
		return SharedTestcontainers.kafka();
	}

	@Bean(destroyMethod = "")
	@ServiceConnection
	MySQLContainer mysqlContainer() {
		return SharedTestcontainers.mysql();
	}

	@Bean(destroyMethod = "")
	@ServiceConnection(name = "redis")
	GenericContainer<?> redisContainer() {
		return SharedTestcontainers.redis();
	}

}
