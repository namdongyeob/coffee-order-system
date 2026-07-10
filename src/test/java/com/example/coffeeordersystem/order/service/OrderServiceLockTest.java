// 주문 서비스의 사용자별 Redisson 진입 락 계약을 검증합니다.
package com.example.coffeeordersystem.order.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import com.example.coffeeordersystem.menu.repository.MenuRepository;
import com.example.coffeeordersystem.order.repository.OrderRepository;
import com.example.coffeeordersystem.order.dto.OrderResponse;
import com.example.coffeeordersystem.order.domain.OrderStatus;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class OrderServiceLockTest {

	@Mock
	MenuRepository menuRepository;

	@Mock
	UserPointRepository userPointRepository;

	@Mock
	OrderRepository orderRepository;

	@Mock
	RedissonClient redissonClient;

	@Mock
	RLock lock;

	@Mock
	TransactionTemplate transactionTemplate;

	@InjectMocks
	OrderService orderService;

	@Test
	void createOrderFailsWithConflictWhenUserLockCannotBeAcquired() throws Exception {
		when(redissonClient.getLock("lock:order:user:7")).thenReturn(lock);
		when(lock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(false);

		assertThatThrownBy(() -> orderService.createOrder(7L, 1L))
				.isInstanceOf(ApiException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ORDER_LOCK_NOT_ACQUIRED);
		verify(transactionTemplate, never()).execute(any());
		verify(lock, never()).unlock();
	}

	@Test
	void createOrderUsesUserLockAroundTransactionAndUnlocksAfterSuccess() throws Exception {
		OrderResponse expected = new OrderResponse(1L, 7L, 1L, "아메리카노", 4_500, OrderStatus.PAID, LocalDateTime.now());
		when(redissonClient.getLock("lock:order:user:7")).thenReturn(lock);
		when(lock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(true);
		when(lock.isHeldByCurrentThread()).thenReturn(true);
		when(transactionTemplate.execute(any())).thenReturn(expected);

		orderService.createOrder(7L, 1L);

		verify(transactionTemplate).execute(any());
		verify(lock).unlock();
	}

	@Test
	void createOrderUnlocksWhenTransactionFails() throws Exception {
		when(redissonClient.getLock("lock:order:user:7")).thenReturn(lock);
		when(lock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(true);
		when(lock.isHeldByCurrentThread()).thenReturn(true);
		when(transactionTemplate.execute(any())).thenThrow(new ApiException(ErrorCode.INSUFFICIENT_POINT));

		assertThatThrownBy(() -> orderService.createOrder(7L, 1L))
				.isInstanceOf(ApiException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INSUFFICIENT_POINT);
		verify(lock).unlock();
	}
}
