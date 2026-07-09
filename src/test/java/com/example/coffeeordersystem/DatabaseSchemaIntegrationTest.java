package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.event.domain.ProcessedEvent;
import com.example.coffeeordersystem.event.repository.ProcessedEventRepository;
import com.example.coffeeordersystem.menu.domain.Menu;
import com.example.coffeeordersystem.menu.repository.MenuRepository;
import com.example.coffeeordersystem.order.domain.Order;
import com.example.coffeeordersystem.order.domain.OrderStatus;
import com.example.coffeeordersystem.order.repository.OrderRepository;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DatabaseSchemaIntegrationTest {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MenuRepository menuRepository;

	@Autowired
	UserPointRepository userPointRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	ProcessedEventRepository processedEventRepository;

	@Test
	void flywayCreatesRequiredTablesAndMenuSeedData() {
		List<String> tableNames = jdbcTemplate.queryForList("""
				select table_name
				from information_schema.tables
				where table_schema = database()
				""", String.class);

		assertThat(tableNames)
				.contains("menu", "user_point", "orders", "processed_event");

		assertThat(menuRepository.findAll())
				.extracting(Menu::getName)
				.containsExactly("아메리카노", "카페라떼", "카푸치노", "에스프레소");
	}

	@Test
	void repositoriesPersistAndReadSchemaBackedEntities() {
		Menu menu = menuRepository.findById(1L).orElseThrow();
		UserPoint userPoint = userPointRepository.save(new UserPoint(1L, 10_000));
		Order order = orderRepository.save(new Order(userPoint.getUserId(), menu, menu.getPrice(), OrderStatus.PAID,
				LocalDateTime.now()));
		ProcessedEvent processedEvent = processedEventRepository.save(
				new ProcessedEvent("event-1", "OrderCompletedEvent", "ranking-consumer-group", LocalDateTime.now()));

		assertThat(orderRepository.findById(order.getId())).isPresent();
		assertThat(userPointRepository.findByUserId(userPoint.getUserId())).isPresent();
		assertThat(processedEventRepository.existsByEventId(processedEvent.getEventId())).isTrue();
	}
}
