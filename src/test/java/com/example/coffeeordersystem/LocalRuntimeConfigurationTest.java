package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class LocalRuntimeConfigurationTest {

	@Test
	void localProfileUsesProjectComposeDefaults() throws IOException {
		ClassPathResource resource = new ClassPathResource("application-local.properties");

		assertThat(resource.exists()).isTrue();
		String properties = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

		assertThat(properties)
				.contains("jdbc:mysql://${LOCAL_MYSQL_HOST:127.0.0.1}:${LOCAL_MYSQL_PORT:13306}")
				.contains("spring.data.redis.host=${LOCAL_REDIS_HOST:127.0.0.1}")
				.contains("spring.data.redis.port=${LOCAL_REDIS_PORT:16379}")
				.contains("${LOCAL_KAFKA_BOOTSTRAP_SERVERS:127.0.0.1:19092}");
	}
}
