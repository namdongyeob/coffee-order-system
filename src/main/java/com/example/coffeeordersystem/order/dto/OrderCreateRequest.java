// 주문 생성 요청 값을 검증하는 DTO입니다.
package com.example.coffeeordersystem.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCreateRequest(
		@NotNull @Positive Long userId,
		@NotNull @Positive Long menuId
) {
}
