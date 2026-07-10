// 주문 생성 HTTP API를 제공하는 컨트롤러입니다.
package com.example.coffeeordersystem.order.controller;

import com.example.coffeeordersystem.order.dto.OrderCreateRequest;
import com.example.coffeeordersystem.order.dto.OrderResponse;
import com.example.coffeeordersystem.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
		OrderResponse response = orderService.createOrder(request.userId(), request.menuId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
