// Testcontainers 종료 뒤 scheduled task가 인프라에 접근하지 않게 막는 테스트 전용 scheduler입니다.
package com.example.coffeeordersystem;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

final class DisabledTaskScheduler implements TaskScheduler {

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		return cancelledFuture();
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
		return cancelledFuture();
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
		return cancelledFuture();
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
		return cancelledFuture();
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
		return cancelledFuture();
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
		return cancelledFuture();
	}

	private ScheduledFuture<?> cancelledFuture() {
		return new CancelledScheduledFuture();
	}

	private static final class CancelledScheduledFuture implements ScheduledFuture<Object> {

		private final FutureTask<Object> future = new FutureTask<>(() -> null);

		private CancelledScheduledFuture() {
			future.cancel(false);
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return 0;
		}

		@Override
		public int compareTo(Delayed other) {
			return 0;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return future.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return future.isCancelled();
		}

		@Override
		public boolean isDone() {
			return future.isDone();
		}

		@Override
		public Object get() throws java.util.concurrent.ExecutionException, InterruptedException {
			return future.get();
		}

		@Override
		public Object get(long timeout, TimeUnit unit)
				throws java.util.concurrent.ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
			return future.get(timeout, unit);
		}
	}
}
