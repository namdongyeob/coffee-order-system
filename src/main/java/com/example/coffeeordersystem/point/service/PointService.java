// 포인트 충전 유스케이스를 처리하는 서비스입니다.
package com.example.coffeeordersystem.point.service;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.dto.PointChargeResponse;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

	private static final int MAX_CHARGE_AMOUNT = 1_000_000;

	private final UserPointRepository userPointRepository;

	@Transactional
	public PointChargeResponse charge(Long userId, int amount) {
		validateChargeAmount(amount);

		UserPoint userPoint = userPointRepository.findByUserIdForUpdate(userId)
				.orElseGet(() -> userPointRepository.save(new UserPoint(userId, 0)));
		userPoint.charge(amount);

		return PointChargeResponse.from(userPoint);
	}

	private void validateChargeAmount(int amount) {
		if (amount < 1 || amount > MAX_CHARGE_AMOUNT) {
			throw new ApiException(ErrorCode.INVALID_CHARGE_AMOUNT);
		}
	}
}
