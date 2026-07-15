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
import com.example.coffeeordersystem.order.event.OutboxEvent;
import com.example.coffeeordersystem.order.event.OutboxEventRepository;
import com.example.coffeeordersystem.order.repository.OrderRepository;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

	private final MenuRepository menuRepository;
	private final UserPointRepository userPointRepository;
	private final OrderRepository orderRepository;
	private final RedissonClient redissonClient;
	private final TransactionTemplate transactionTemplate;
	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	public OrderResponse createOrder(Long userId, Long menuId) {
		String lockKey = "lock:order:user:" + userId;
		RLock lock = redissonClient.getLock(lockKey);
		boolean acquired = false;
		try {
			acquired = lock.tryLock(2, 5, TimeUnit.SECONDS);
			if (!acquired) {
				throw new ApiException(ErrorCode.ORDER_LOCK_NOT_ACQUIRED);
			}
			return transactionTemplate.execute(status -> payAndCreateOrder(userId, menuId));
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new ApiException(ErrorCode.ORDER_LOCK_NOT_ACQUIRED);
		} finally {
			releaseLockSafely(lock, userId, lockKey, acquired);
		}
	}

	private void releaseLockSafely(RLock lock, Long userId, String lockKey, boolean acquired) {
		if (!acquired) {
			return;
		}

		try {
			if (!lock.isHeldByCurrentThread()) {
				return;
			}
		} catch (RuntimeException exception) {
			log.warn("Order lock cleanup failed. userId={}, lockKey={}, stage=isHeldByCurrentThread", userId, lockKey, exception);
			return;
		}

		try {
			lock.unlock();
		} catch (RuntimeException exception) {
			log.warn("Order lock cleanup failed. userId={}, lockKey={}, stage=unlock", userId, lockKey, exception);
		}
	}

	private OrderResponse payAndCreateOrder(Long userId, Long menuId) {
		Menu menu = menuRepository.findById(menuId)
				.orElseThrow(() -> new ApiException(ErrorCode.MENU_NOT_FOUND));
		UserPoint userPoint = userPointRepository.findByUserIdForUpdate(userId)
				.orElseThrow(() -> new ApiException(ErrorCode.USER_POINT_NOT_FOUND));

		userPoint.pay(menu.getPrice());
		Order order = orderRepository.save(new Order(userId, menu, menu.getPrice(), OrderStatus.PAID, LocalDateTime.now()));
		OrderResponse response = OrderResponse.from(order);

		saveOutboxEvent(response);

		return response;
	}

	// 주문 트랜잭션과 같은 경계에서 OutboxEvent를 저장해, Kafka 발행이 실패해도 이벤트가 유실되지 않도록 한다.
	// 실제 발행은 별도 OutboxEventPublisher가 담당한다.
	private void saveOutboxEvent(OrderResponse response) {
		OrderCompletedEvent event = new OrderCompletedEvent(
				UUID.randomUUID(),
				response.orderId(),
				response.userId(),
				response.menuId(),
				response.paidAmount(),
				response.orderedAt()
		);
		String payload;
		try {
			payload = objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("주문 완료 이벤트 직렬화에 실패했습니다.", exception);
		}
		outboxEventRepository.save(new OutboxEvent(
				event.eventId().toString(), OrderEventPublisher.ORDER_COMPLETED_TOPIC, payload, LocalDateTime.now()));
	}
}
