// 테스트에서 scheduled task 실행을 막는 scheduler 계약을 검증합니다.
package com.example.coffeeordersystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

class DisabledTaskSchedulerTest {

	@Test
	void acceptsScheduledTaskWithoutExecutingIt() {
		TaskScheduler scheduler = new DisabledTaskScheduler();
		AtomicBoolean executed = new AtomicBoolean();
		ScheduledFuture<?> scheduled = scheduler.schedule(() -> executed.set(true), Instant.now());

		assertThat(scheduled.isCancelled()).isTrue();
		assertThat(executed).isFalse();
	}

	@Test
	void cancelsTriggerAndPeriodicSchedulesWithoutExecutingThem() {
		TaskScheduler scheduler = new DisabledTaskScheduler();
		AtomicBoolean executed = new AtomicBoolean();
		Runnable task = () -> executed.set(true);
		Trigger trigger = triggerContext -> Instant.now();

		assertCancelled(scheduler.schedule(task, trigger));
		assertCancelled(scheduler.scheduleAtFixedRate(task, Instant.now(), Duration.ofSeconds(1)));
		assertCancelled(scheduler.scheduleAtFixedRate(task, Duration.ofSeconds(1)));
		assertCancelled(scheduler.scheduleWithFixedDelay(task, Instant.now(), Duration.ofSeconds(1)));
		assertCancelled(scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(1)));

		assertThat(executed).isFalse();
	}

	private void assertCancelled(ScheduledFuture<?> scheduled) {
		assertThat(scheduled.isCancelled()).isTrue();
	}
}
