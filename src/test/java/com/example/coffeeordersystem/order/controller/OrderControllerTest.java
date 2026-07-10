// 주문 생성 API 계약을 검증하는 컨트롤러 테스트입니다.
package com.example.coffeeordersystem.order.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import com.example.coffeeordersystem.order.domain.OrderStatus;
import com.example.coffeeordersystem.order.dto.OrderResponse;
import com.example.coffeeordersystem.order.service.OrderService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void createOrderReturnsCreatedOrder() throws Exception {
		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"userId": 1, "menuId": 1}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().json("""
						{
						  "orderId": 100,
						  "userId": 1,
						  "menuId": 1,
						  "menuName": "아메리카노",
						  "paidAmount": 4500,
						  "status": "PAID",
						  "orderedAt": "2026-07-09T10:00:00"
						}
						"""));
	}

	@Test
	void createOrderReturnsNotFoundWhenMenuDoesNotExist() throws Exception {
		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"userId": 1, "menuId": 404}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().json("""
						{"code": "MENU_NOT_FOUND", "message": "메뉴를 찾을 수 없습니다."}
						"""));
	}

	@Test
	void createOrderReturnsNotFoundWhenUserPointDoesNotExist() throws Exception {
		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"userId": 404, "menuId": 1}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().json("""
						{"code": "USER_POINT_NOT_FOUND", "message": "사용자 포인트를 찾을 수 없습니다."}
						"""));
	}

	@Test
	void createOrderReturnsConflictWhenBalanceIsInsufficient() throws Exception {
		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"userId": 2, "menuId": 1}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().json("""
						{"code": "INSUFFICIENT_POINT", "message": "포인트 잔액이 부족합니다."}
						"""));
	}

	@Test
	void createOrderReturnsConflictWhenUserLockCannotBeAcquired() throws Exception {
		mockMvc.perform(post("/api/orders")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"userId": 3, "menuId": 1}
							"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ORDER_LOCK_NOT_ACQUIRED"));
	}

	@TestConfiguration
	static class OrderControllerTestConfig {

		@Bean
		OrderService orderService() {
			OrderService orderService = mock(OrderService.class);
			when(orderService.createOrder(eq(1L), eq(1L))).thenReturn(new OrderResponse(
					100L,
					1L,
					1L,
					"아메리카노",
					4_500,
					OrderStatus.PAID,
					LocalDateTime.of(2026, 7, 9, 10, 0)
			));
			when(orderService.createOrder(eq(1L), eq(404L))).thenThrow(new ApiException(ErrorCode.MENU_NOT_FOUND));
			when(orderService.createOrder(eq(404L), eq(1L))).thenThrow(new ApiException(ErrorCode.USER_POINT_NOT_FOUND));
			when(orderService.createOrder(eq(2L), eq(1L))).thenThrow(new ApiException(ErrorCode.INSUFFICIENT_POINT));
			when(orderService.createOrder(eq(3L), eq(1L))).thenThrow(new ApiException(ErrorCode.ORDER_LOCK_NOT_ACQUIRED));
			return orderService;
		}
	}
}
