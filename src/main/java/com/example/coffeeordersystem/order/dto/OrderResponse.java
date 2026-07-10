// 주문 생성 결과를 API 응답으로 전달하는 DTO입니다.
package com.example.coffeeordersystem.order.dto;

import com.example.coffeeordersystem.order.domain.Order;
import com.example.coffeeordersystem.order.domain.OrderStatus;
import java.time.LocalDateTime;

public record OrderResponse(
		Long orderId,
		Long userId,
		Long menuId,
		String menuName,
		int paidAmount,
		OrderStatus status,
		LocalDateTime orderedAt
) {

	public static OrderResponse from(Order order) {
		return new OrderResponse(
				order.getId(),
				order.getUserId(),
				order.getMenu().getId(),
				order.getMenu().getName(),
				order.getPaidAmount(),
				order.getStatus(),
				order.getOrderedAt()
		);
	}
}
