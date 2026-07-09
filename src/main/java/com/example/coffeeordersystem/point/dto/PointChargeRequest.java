// 포인트 충전 요청 값을 검증하는 DTO입니다.
package com.example.coffeeordersystem.point.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PointChargeRequest(
		@NotNull @Positive Long userId,
		@NotNull @Min(1) @Max(1_000_000) Integer amount
) {
}
