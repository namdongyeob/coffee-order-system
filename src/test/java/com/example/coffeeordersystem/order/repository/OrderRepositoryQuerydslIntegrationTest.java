// 주문 DB 원천 QueryDSL 검증 조회를 MySQL 통합 환경에서 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coffeeordersystem.menu.domain.Menu;
import com.example.coffeeordersystem.menu.repository.MenuRepository;
import com.example.coffeeordersystem.order.domain.Order;
import com.example.coffeeordersystem.order.domain.OrderStatus;
import com.example.coffeeordersystem.order.repository.OrderRepository;
import com.example.coffeeordersystem.order.repository.PaidMenuOrderCount;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class OrderRepositoryQuerydslIntegrationTest {

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	MenuRepository menuRepository;

	@Autowired
	UserPointRepository userPointRepository;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void findsTopThreePaidMenuOrderCountsForTheRequestedPeriodFromMysql() {
		LocalDateTime from = LocalDateTime.of(2026, 7, 1, 0, 0);
		LocalDateTime to = from.plusDays(1);
		Menu first = menuRepository.save(new Menu("QueryDSL 첫 번째 메뉴", 4_500));
		Menu second = menuRepository.save(new Menu("QueryDSL 두 번째 메뉴", 5_000));
		Menu third = menuRepository.save(new Menu("QueryDSL 세 번째 메뉴", 5_500));
		Menu outsidePeriod = menuRepository.save(new Menu("QueryDSL 기간 외 메뉴", 6_000));
		UserPoint userPoint = userPointRepository.save(new UserPoint(1601L, 100_000));

		saveOrders(userPoint, first, 4, from.plusHours(1));
		saveOrders(userPoint, second, 3, from.plusHours(2));
		saveOrders(userPoint, third, 2, from.plusHours(3));
		saveOrders(userPoint, outsidePeriod, 10, to);

		List<PaidMenuOrderCount> results = orderRepository.findTopPaidMenuOrderCounts(from, to);

		assertThat(results).containsExactly(
				new PaidMenuOrderCount(first.getId(), 4L),
				new PaidMenuOrderCount(second.getId(), 3L),
				new PaidMenuOrderCount(third.getId(), 2L));
	}

	@Test
	void runsExplainForThePaidMenuAggregationAgainstMysql() {
		LocalDateTime from = LocalDateTime.of(2026, 7, 2, 0, 0);
		LocalDateTime to = from.plusDays(1);
		Menu menu = menuRepository.save(new Menu("EXPLAIN 검증 메뉴", 4_500));
		UserPoint userPoint = userPointRepository.save(new UserPoint(1602L, 100_000));
		saveOrders(userPoint, menu, 10, from.plusHours(1));

		var plan = jdbcTemplate.queryForMap("""
				EXPLAIN
				SELECT menu_id, COUNT(*) AS order_count
				FROM orders
				WHERE status = 'PAID'
				  AND ordered_at >= ?
				  AND ordered_at < ?
				GROUP BY menu_id
				ORDER BY COUNT(*) DESC
				LIMIT 3
				""", from, to);

		System.out.println("Issue #16 EXPLAIN plan: " + plan);
		assertThat(plan)
				.containsEntry("table", "orders")
				.containsKey("key")
				.containsKey("type")
				.containsKey("Extra");
	}

	private void saveOrders(UserPoint userPoint, Menu menu, int count, LocalDateTime orderedAt) {
		for (int index = 0; index < count; index++) {
			orderRepository.save(new Order(userPoint.getUserId(), menu, menu.getPrice(), OrderStatus.PAID, orderedAt));
		}
	}
}
