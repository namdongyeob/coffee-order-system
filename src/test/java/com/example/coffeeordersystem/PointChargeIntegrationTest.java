// 포인트 충전 서비스의 DB 정합성을 검증하는 통합 테스트입니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.coffeeordersystem.point.domain.UserPoint;
import com.example.coffeeordersystem.point.dto.PointChargeResponse;
import com.example.coffeeordersystem.point.repository.UserPointRepository;
import com.example.coffeeordersystem.point.service.PointService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PointChargeIntegrationTest {

	private ExecutorService executorService;

	@Autowired
	PointService pointService;

	@Autowired
	UserPointRepository userPointRepository;

	@BeforeEach
	void setUp() {
		executorService = Executors.newFixedThreadPool(10);
	}

	@AfterEach
	void tearDown() {
		executorService.shutdownNow();
	}

	@Test
	void chargeCreatesUserPointWhenRowDoesNotExist() {
		PointChargeResponse response = pointService.charge(10L, 10_000);

		assertThat(response.userId()).isEqualTo(10L);
		assertThat(response.balance()).isEqualTo(10_000);
		assertThat(userPointRepository.findByUserId(10L))
				.get()
				.extracting(UserPoint::getBalance)
				.isEqualTo(10_000);
	}

	@Test
	void chargeAddsAmountToExistingBalance() {
		userPointRepository.save(new UserPoint(11L, 5_000));

		PointChargeResponse response = pointService.charge(11L, 10_000);

		assertThat(response.userId()).isEqualTo(11L);
		assertThat(response.balance()).isEqualTo(15_000);
		assertThat(userPointRepository.findByUserId(11L))
				.get()
				.extracting(UserPoint::getBalance)
				.isEqualTo(15_000);
	}

	@Test
	void chargeRejectsAmountOverPolicyLimit() {
		assertThatThrownBy(() -> pointService.charge(12L, 1_000_001))
				.hasMessage("유효하지 않은 포인트 충전 요청입니다.");

		assertThat(userPointRepository.findByUserId(12L)).isEmpty();
	}

	@Test
	void concurrentChargesPreserveEveryAmountForExistingUserPoint() throws Exception {
		userPointRepository.save(new UserPoint(21L, 1_000));

		List<PointChargeResponse> responses = chargeConcurrently(21L, 100, 10);

		assertThat(responses).hasSize(10);
		assertThat(userPointRepository.findByUserId(21L))
				.get()
				.extracting(UserPoint::getBalance)
				.isEqualTo(2_000);
	}

	@Test
	void concurrentFirstChargesCreateOneRowAndPreserveEveryAmount() throws Exception {
		List<PointChargeResponse> responses = chargeConcurrently(22L, 100, 10);

		assertThat(responses).hasSize(10);
		assertThat(userPointRepository.findByUserId(22L))
				.get()
				.extracting(UserPoint::getBalance)
				.isEqualTo(1_000);
	}

	private List<PointChargeResponse> chargeConcurrently(Long userId, int amount, int requestCount) throws Exception {
		CountDownLatch ready = new CountDownLatch(requestCount);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<PointChargeResponse>> futures = new ArrayList<>();

		for (int i = 0; i < requestCount; i++) {
			futures.add(executorService.submit(() -> {
				ready.countDown();
				start.await();
				return pointService.charge(userId, amount);
			}));
		}

		ready.await();
		start.countDown();

		List<PointChargeResponse> responses = new ArrayList<>();
		for (Future<PointChargeResponse> future : futures) {
			responses.add(future.get());
		}
		return responses;
	}
}
