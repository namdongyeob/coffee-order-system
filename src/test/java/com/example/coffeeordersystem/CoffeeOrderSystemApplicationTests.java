package com.example.coffeeordersystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "ranking.consumer.enabled=false")
class CoffeeOrderSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
