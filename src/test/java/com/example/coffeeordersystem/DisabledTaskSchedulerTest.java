// 테스트에서 scheduled task 실행을 막는 scheduler 계약을 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;

class DisabledTaskSchedulerTest {

	@Test
	void acceptsScheduledTaskWithoutExecutingIt() {
		TaskScheduler scheduler = new DisabledTaskScheduler();
		AtomicBoolean executed = new AtomicBoolean();
		ScheduledFuture<?> scheduled = scheduler.schedule(() -> executed.set(true), Instant.now());

		assertThat(scheduled.isCancelled()).isTrue();
		assertThat(executed).isFalse();
	}
}
