// 실제 Redis에서 사용자 주문 락 경합과 획득 실패를 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import com.example.coffeeordersystem.order.service.OrderService;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RedisOrderLockIntegrationTest {

	@Autowired
	OrderService orderService;

	@Autowired
	RedissonClient redissonClient;

	@Test
	void createOrderFailsAfterWaitTimeWhenAnotherThreadHoldsUserLock() throws Exception {
		RLock lock = redissonClient.getLock("lock:order:user:700");
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CountDownLatch locked = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		Future<?> holder = executorService.submit(() -> {
			lock.lock(10, TimeUnit.SECONDS);
			try {
				locked.countDown();
				if (!release.await(10, TimeUnit.SECONDS)) {
					throw new IllegalStateException("Timed out waiting to release test lock");
				}
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(exception);
			} finally {
				if (lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
		});

		try {
			assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
			Instant startedAt = Instant.now();

			assertThatThrownBy(() -> orderService.createOrder(700L, 1L))
					.isInstanceOf(ApiException.class)
					.extracting("errorCode")
					.isEqualTo(ErrorCode.ORDER_LOCK_NOT_ACQUIRED);

			assertThat(Duration.between(startedAt, Instant.now()))
					.isBetween(Duration.ofMillis(1_500), Duration.ofSeconds(4));
		} finally {
			release.countDown();
			holder.get(5, TimeUnit.SECONDS);
			executorService.shutdownNow();
			assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
		}
	}
}
