// 포인트 충전 HTTP API를 제공하는 컨트롤러입니다.
package com.example.coffeeordersystem.point.controller;

import com.example.coffeeordersystem.point.dto.PointChargeRequest;
import com.example.coffeeordersystem.point.dto.PointChargeResponse;
import com.example.coffeeordersystem.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

	private final PointService pointService;

	@PostMapping("/charge")
	public ResponseEntity<PointChargeResponse> charge(@Valid @RequestBody PointChargeRequest request) {
		PointChargeResponse response = pointService.charge(request.userId(), request.amount());
		return ResponseEntity.ok(response);
	}
}
