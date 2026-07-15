// 주문 서비스의 사용자별 Redisson 진입 락 계약을 검증합니다.
package com.example.coffeeordersystem.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import com.example.coffeeordersystem.menu.repository.MenuRepository;
import com.example.coffeeordersystem.order.domain.OrderStatus;
import com.example.coffeeordersystem.order.dto.OrderResponse;
import com.example.coffeeordersystem.order.event.OutboxEventRepository;
import com.example.coffeeordersystem.order.repository.OrderRepository;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
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

	@Mock
	OutboxEventRepository outboxEventRepository;

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
	void createOrderReturnsResponseAfterTransactionTemplateAndThenUnlocks() throws Exception {
		OrderResponse expected = new OrderResponse(1L, 7L, 1L, "아메리카노", 4_500, OrderStatus.PAID, LocalDateTime.now());
		when(redissonClient.getLock("lock:order:user:7")).thenReturn(lock);
		when(lock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(true);
		when(lock.isHeldByCurrentThread()).thenReturn(true);
		when(transactionTemplate.execute(any())).thenReturn(expected);

		OrderResponse actual = orderService.createOrder(7L, 1L);

		assertThat(actual).isEqualTo(expected);
		InOrder order = inOrder(transactionTemplate, lock);
		order.verify(transactionTemplate).execute(any());
		order.verify(lock).unlock();
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

	@Test
	void createOrderReturnsCommittedResponseWhenLockOwnershipCheckFails() throws Exception {
		OrderResponse expected = new OrderResponse(1L, 7L, 1L, "아메리카노", 4_500, OrderStatus.PAID, LocalDateTime.now());
		when(redissonClient.getLock("lock:order:user:7")).thenReturn(lock);
		when(lock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(true);
		when(lock.isHeldByCurrentThread()).thenThrow(new IllegalStateException("Redis unavailable"));
		when(transactionTemplate.execute(any())).thenReturn(expected);

		assertThat(orderService.createOrder(7L, 1L)).isEqualTo(expected);
		verify(lock, never()).unlock();
	}

	@Test
	void createOrderReturnsCommittedResponseWhenUnlockFails() throws Exception {
		OrderResponse expected = new OrderResponse(1L, 7L, 1L, "아메리카노", 4_500, OrderStatus.PAID, LocalDateTime.now());
		when(redissonClient.getLock("lock:order:user:7")).thenReturn(lock);
		when(lock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(true);
		when(lock.isHeldByCurrentThread()).thenReturn(true);
		doThrow(new IllegalStateException("Redis unavailable")).when(lock).unlock();
		when(transactionTemplate.execute(any())).thenReturn(expected);

		assertThat(orderService.createOrder(7L, 1L)).isEqualTo(expected);
	}

	@Test
	void createOrderPreservesBusinessExceptionWhenLockOwnershipCheckFails() throws Exception {
		when(redissonClient.getLock("lock:order:user:7")).thenReturn(lock);
		when(lock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(true);
		when(lock.isHeldByCurrentThread()).thenThrow(new IllegalStateException("Redis unavailable"));
		when(transactionTemplate.execute(any())).thenThrow(new ApiException(ErrorCode.INSUFFICIENT_POINT));

		assertThatThrownBy(() -> orderService.createOrder(7L, 1L))
				.isInstanceOf(ApiException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INSUFFICIENT_POINT);
		verify(lock, never()).unlock();
	}

	@Test
	void createOrderPreservesBusinessExceptionWhenUnlockFails() throws Exception {
		when(redissonClient.getLock("lock:order:user:7")).thenReturn(lock);
		when(lock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(true);
		when(lock.isHeldByCurrentThread()).thenReturn(true);
		doThrow(new IllegalStateException("Redis unavailable")).when(lock).unlock();
		when(transactionTemplate.execute(any())).thenThrow(new ApiException(ErrorCode.INSUFFICIENT_POINT));

		assertThatThrownBy(() -> orderService.createOrder(7L, 1L))
				.isInstanceOf(ApiException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INSUFFICIENT_POINT);
	}

	@Test
	void createOrderDoesNotUnlockWhenCurrentThreadDoesNotOwnLock() throws Exception {
		OrderResponse expected = new OrderResponse(1L, 7L, 1L, "아메리카노", 4_500, OrderStatus.PAID, LocalDateTime.now());
		when(redissonClient.getLock("lock:order:user:7")).thenReturn(lock);
		when(lock.tryLock(2, 5, TimeUnit.SECONDS)).thenReturn(true);
		when(lock.isHeldByCurrentThread()).thenReturn(false);
		when(transactionTemplate.execute(any())).thenReturn(expected);

		assertThat(orderService.createOrder(7L, 1L)).isEqualTo(expected);
		verify(lock, never()).unlock();
	}
}
