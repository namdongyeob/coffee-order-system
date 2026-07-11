// 주문 생성과 포인트 차감 트랜잭션을 처리하는 서비스입니다.
package com.example.coffeeordersystem.order.service;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import com.example.coffeeordersystem.menu.domain.Menu;
import com.example.coffeeordersystem.menu.repository.MenuRepository;
import com.example.coffeeordersystem.order.domain.Order;
import com.example.coffeeordersystem.order.domain.OrderStatus;
import com.example.coffeeordersystem.order.dto.OrderResponse;
import com.example.coffeeordersystem.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.order.event.OrderEventPublisher;
import com.example.coffeeordersystem.order.repository.OrderRepository;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final MenuRepository menuRepository;
	private final UserPointRepository userPointRepository;
	private final OrderRepository orderRepository;
	private final RedissonClient redissonClient;
	private final TransactionTemplate transactionTemplate;
	private final OrderEventPublisher orderEventPublisher;

	public OrderResponse createOrder(Long userId, Long menuId) {
		RLock lock = redissonClient.getLock("lock:order:user:" + userId);
		boolean acquired = false;
		try {
			acquired = lock.tryLock(2, 5, TimeUnit.SECONDS);
			if (!acquired) {
				throw new ApiException(ErrorCode.ORDER_LOCK_NOT_ACQUIRED);
			}
			OrderResponse response = transactionTemplate.execute(status -> payAndCreateOrder(userId, menuId));
			orderEventPublisher.publish(new OrderCompletedEvent(
					UUID.randomUUID(),
					response.orderId(),
					response.userId(),
					response.menuId(),
					response.paidAmount(),
					response.orderedAt()
			));
			return response;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new ApiException(ErrorCode.ORDER_LOCK_NOT_ACQUIRED);
		} finally {
			if (acquired && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private OrderResponse payAndCreateOrder(Long userId, Long menuId) {
		Menu menu = menuRepository.findById(menuId)
				.orElseThrow(() -> new ApiException(ErrorCode.MENU_NOT_FOUND));
		UserPoint userPoint = userPointRepository.findByUserIdForUpdate(userId)
				.orElseThrow(() -> new ApiException(ErrorCode.USER_POINT_NOT_FOUND));

		userPoint.pay(menu.getPrice());
		Order order = orderRepository.save(new Order(userId, menu, menu.getPrice(), OrderStatus.PAID, LocalDateTime.now()));

		return OrderResponse.from(order);
	}
}
