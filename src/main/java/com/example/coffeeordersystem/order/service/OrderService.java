// 주문 생성과 포인트 차감 트랜잭션을 처리하는 서비스입니다.
package com.example.coffeeordersystem.order.service;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import com.example.coffeeordersystem.menu.domain.Menu;
import com.example.coffeeordersystem.menu.repository.MenuRepository;
import com.example.coffeeordersystem.order.domain.Order;
import com.example.coffeeordersystem.order.domain.OrderStatus;
import com.example.coffeeordersystem.order.dto.OrderResponse;
import com.example.coffeeordersystem.order.repository.OrderRepository;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final MenuRepository menuRepository;
	private final UserPointRepository userPointRepository;
	private final OrderRepository orderRepository;

	@Transactional
	public OrderResponse createOrder(Long userId, Long menuId) {
		Menu menu = menuRepository.findById(menuId)
				.orElseThrow(() -> new ApiException(ErrorCode.MENU_NOT_FOUND));
		UserPoint userPoint = userPointRepository.findByUserIdForUpdate(userId)
				.orElseThrow(() -> new ApiException(ErrorCode.USER_POINT_NOT_FOUND));

		userPoint.pay(menu.getPrice());
		Order order = orderRepository.save(new Order(userId, menu, menu.getPrice(), OrderStatus.PAID, LocalDateTime.now()));

		return OrderResponse.from(order);
	}
}
