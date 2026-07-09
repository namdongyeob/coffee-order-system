// 포인트 충전 후 잔액을 반환하는 DTO입니다.
package com.example.coffeeordersystem.point.dto;

import com.example.coffeeordersystem.point.domain.UserPoint;

public record PointChargeResponse(Long userId, int balance) {

	public static PointChargeResponse from(UserPoint userPoint) {
		return new PointChargeResponse(userPoint.getUserId(), userPoint.getBalance());
	}
}
