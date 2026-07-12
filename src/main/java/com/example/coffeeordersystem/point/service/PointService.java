// 포인트 충전 유스케이스를 처리하는 서비스입니다.
package com.example.coffeeordersystem.point.service;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.dto.PointChargeResponse;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PointService {

	private static final int MAX_CHARGE_AMOUNT = 1_000_000;
	private static final int MAX_CONCURRENCY_RETRIES = 3;

	private final UserPointRepository userPointRepository;
	private final TransactionOperations chargeTransaction;

	public PointService(UserPointRepository userPointRepository, PlatformTransactionManager transactionManager) {
		this.userPointRepository = userPointRepository;
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		this.chargeTransaction = transactionTemplate;
	}

	public PointChargeResponse charge(Long userId, int amount) {
		validateChargeAmount(amount);

		for (int attempt = 0; attempt <= MAX_CONCURRENCY_RETRIES; attempt++) {
			try {
				return chargeTransaction.execute(status -> chargeInTransaction(userId, amount));
			}
			catch (CannotAcquireLockException | DataIntegrityViolationException exception) {
				if (attempt == MAX_CONCURRENCY_RETRIES) {
					throw exception;
				}
			}
		}
		throw new IllegalStateException("포인트 충전 재시도 상태가 올바르지 않습니다.");
	}

	private PointChargeResponse chargeInTransaction(Long userId, int amount) {
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
