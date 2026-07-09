// 포인트 충전 API 계약을 검증하는 컨트롤러 테스트입니다.
package com.example.coffeeordersystem.point.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.coffeeordersystem.point.dto.PointChargeResponse;
import com.example.coffeeordersystem.point.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
class PointControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void chargePointReturnsChargedBalance() throws Exception {
		mockMvc.perform(post("/api/points/charge")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"userId": 1, "amount": 10000}
								"""))
				.andExpect(status().isOk())
				.andExpect(content().json("""
						{"userId": 1, "balance": 10000}
						"""));
	}

	@Test
	void chargePointRejectsMissingUserId() throws Exception {
		mockMvc.perform(post("/api/points/charge")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount": 10000}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("""
						{"code": "INVALID_CHARGE_AMOUNT", "message": "유효하지 않은 포인트 충전 요청입니다."}
						"""));
	}

	@Test
	void chargePointRejectsAmountLessThanOne() throws Exception {
		mockMvc.perform(post("/api/points/charge")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"userId": 1, "amount": 0}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("""
						{"code": "INVALID_CHARGE_AMOUNT", "message": "유효하지 않은 포인트 충전 요청입니다."}
						"""));
	}

	@Test
	void chargePointRejectsAmountOverPolicyLimit() throws Exception {
		mockMvc.perform(post("/api/points/charge")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"userId": 1, "amount": 1000001}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("""
						{"code": "INVALID_CHARGE_AMOUNT", "message": "유효하지 않은 포인트 충전 요청입니다."}
						"""));
	}

	@TestConfiguration
	static class PointControllerTestConfig {

		@Bean
		PointService pointService() {
			PointService pointService = mock(PointService.class);
			when(pointService.charge(eq(1L), eq(10_000))).thenReturn(new PointChargeResponse(1L, 10_000));
			return pointService;
		}
	}
}
