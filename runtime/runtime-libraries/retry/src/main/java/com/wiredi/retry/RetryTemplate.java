package com.wiredi.retry;

import com.wiredi.lang.ThrowingRunnable;
import com.wiredi.lang.ThrowingSupplier;
import com.wiredi.retry.exception.RetryFailedException;
import com.wiredi.retry.policy.RetryPolicy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RetryTemplate {

	private final RetryPolicy retryPolicy;
	private final BackOffStrategy backOffStrategy;

	public RetryTemplate(RetryPolicy retryPolicy, BackOffStrategy backOffStrategy) {
		this.retryPolicy = retryPolicy;
		this.backOffStrategy = backOffStrategy;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public <T> T get(ThrowingSupplier<T> supplier) {
		return doExecute(supplier);
	}

	public <T> T safeGet(Supplier<T> supplier) {
		return get(ThrowingSupplier.wrap(supplier));
	}

	public void execute(ThrowingRunnable runnable) {
		doExecute(ThrowingSupplier.wrap(runnable));
	}

	private <T> T doExecute(ThrowingSupplier<T> supplier) {
		List<Throwable> errors = new ArrayList<>();
		RetryState retryState = retryPolicy.newRetryState();

		while (retryState.isActive()) {
			retryState.sleep();
			try {
				return supplier.get();
			} catch (Throwable throwable) {
				errors.add(throwable);
				Duration nextTimeout = backOffStrategy.next(retryState.timeout());
				retryState = retryState.next(nextTimeout);
			}
		}

		throw new RetryFailedException(retryState, errors);
	}

	public static class Builder {
		private final RetryPolicy retryPolicy = new RetryPolicy();
		private BackOffStrategy backOffStrategy = new FixedBackOffStrategy(Duration.ZERO);

		public Builder withFixedTimeout(Duration duration) {
			backOffStrategy = BackOffStrategy.fixed(duration);
			return this;
		}

		public Builder withFixedTimeout(long timeout, TimeUnit timeUnit) {
			return withFixedTimeout(Duration.of(timeout, timeUnit.toChronoUnit()));
		}

		public Builder withExponentialBackoff(double increment) {
			backOffStrategy = new ExponentialBackOffStrategy(increment);
			return this;
		}

		public Builder withMaxTries(long maxTries) {
			retryPolicy.setMaxAttempts(maxTries);
			return this;
		}

		public Builder retryFor(Class<? extends Throwable> type) {
			retryPolicy.retryOnException(type);
			return this;
		}

		public Builder doNotRetryFor(Class<? extends Throwable> type) {
			retryPolicy.doNotRetryOnException(type);
			return this;
		}

		public RetryTemplate build() {
			return new RetryTemplate(retryPolicy, backOffStrategy);
		}
	}
}
