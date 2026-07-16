// 주문 결제의 DB 트랜잭션 정합성을 검증하는 통합 테스트입니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import com.example.coffeeordersystem.order.domain.Order;
import com.example.coffeeordersystem.order.domain.OrderStatus;
import com.example.coffeeordersystem.order.dto.OrderResponse;
import com.example.coffeeordersystem.order.repository.OrderRepository;
import com.example.coffeeordersystem.order.service.OrderService;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "ranking.consumer.enabled=false")
class OrderPaymentIntegrationTest {

	@Autowired
	OrderService orderService;

	@Autowired
	UserPointRepository userPointRepository;

	@Autowired
	OrderRepository orderRepository;

	@Test
	void createOrderPaysPointAndSavesPaidOrderInSingleTransaction() {
		userPointRepository.save(new UserPoint(101L, 10_000));

		OrderResponse response = orderService.createOrder(101L, 1L);

		assertThat(response.userId()).isEqualTo(101L);
		assertThat(response.menuId()).isEqualTo(1L);
		assertThat(response.paidAmount()).isEqualTo(4_500);
		assertThat(response.status()).isEqualTo(OrderStatus.PAID);
		assertThat(response.orderedAt()).isNotNull();
		assertThat(userPointRepository.findByUserId(101L))
				.get()
				.extracting(UserPoint::getBalance)
				.isEqualTo(5_500);
		assertThat(orderRepository.findById(response.orderId()))
				.get()
				.extracting(Order::getUserId, Order::getPaidAmount, Order::getStatus)
				.containsExactly(101L, 4_500, OrderStatus.PAID);
	}

	@Test
	void createOrderKeepsBalanceAndDoesNotSaveOrderWhenBalanceIsInsufficient() {
		userPointRepository.save(new UserPoint(102L, 1_000));
		long orderCountBefore = orderRepository.count();

		assertThatThrownBy(() -> orderService.createOrder(102L, 1L))
				.isInstanceOf(ApiException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INSUFFICIENT_POINT);

		assertThat(userPointRepository.findByUserId(102L))
				.get()
				.extracting(UserPoint::getBalance)
				.isEqualTo(1_000);
		assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
	}

	@Test
	void createOrderFailsWhenUserPointDoesNotExist() {
		long orderCountBefore = orderRepository.count();

		assertThatThrownBy(() -> orderService.createOrder(103L, 1L))
				.isInstanceOf(ApiException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.USER_POINT_NOT_FOUND);

		assertThat(userPointRepository.findByUserId(103L)).isEmpty();
		assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
	}

	@Test
	void createOrderFailsWhenMenuDoesNotExist() {
		userPointRepository.save(new UserPoint(104L, 10_000));
		long orderCountBefore = orderRepository.count();

		assertThatThrownBy(() -> orderService.createOrder(104L, 404L))
				.isInstanceOf(ApiException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.MENU_NOT_FOUND);

		assertThat(userPointRepository.findByUserId(104L))
				.get()
				.extracting(UserPoint::getBalance)
				.isEqualTo(10_000);
		assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
	}

	@Test
	void createOrderWithSameUserConcurrentlyAllowsOnlyOnePaymentWithPessimisticLock() throws Exception {
		userPointRepository.save(new UserPoint(105L, 4_500));
		long orderCountBefore = orderRepository.count();
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		Callable<Object> orderTask = () -> {
			ready.countDown();
			if (!start.await(5, TimeUnit.SECONDS)) {
				throw new IllegalStateException("Timed out waiting for concurrent start");
			}
			try {
				return orderService.createOrder(105L, 1L);
			} catch (ApiException exception) {
				return exception.getErrorCode();
			}
		};

		try {
			Future<Object> first = executorService.submit(orderTask);
			Future<Object> second = executorService.submit(orderTask);
			assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();

			start.countDown();

			List<Object> results = List.of(
					first.get(10, TimeUnit.SECONDS),
					second.get(10, TimeUnit.SECONDS)
			);

			assertThat(results.stream().filter(OrderResponse.class::isInstance).count()).isEqualTo(1);
			assertThat(results.stream().filter(ErrorCode.INSUFFICIENT_POINT::equals).count()).isEqualTo(1);
			assertThat(userPointRepository.findByUserId(105L))
					.get()
					.extracting(UserPoint::getBalance)
					.isEqualTo(0);
			assertThat(orderRepository.count()).isEqualTo(orderCountBefore + 1);
		} finally {
			executorService.shutdownNow();
			assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
		}
	}
}
