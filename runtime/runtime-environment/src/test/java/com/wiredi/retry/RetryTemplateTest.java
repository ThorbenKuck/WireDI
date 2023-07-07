package com.wiredi.retry;

import com.wiredi.lang.SafeReference;
import com.wiredi.lang.time.Timed;
import com.wiredi.retry.exception.RetryFailedException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RetryTemplateTest {

	@Test
	public void test() {
		// Arrange
		RetryTemplate retryTemplate = RetryTemplate.newInstance()
				.withFixedTimeout(100, TimeUnit.MILLISECONDS)
				.withMaxTries(3)
				.retryFor(NullPointerException.class)
				.build();

		NullPointerException exception = new NullPointerException("Test");
		SafeReference<Throwable> raisedException = new SafeReference<>();

		// Act
		var timed = Timed.of(() -> {
			try {
				retryTemplate.execute(() -> {
					throw exception;
				});
			} catch (Throwable e) {
				raisedException.set(e);
			}
		});

		// Assert
		assertThat(timed.get(TimeUnit.MILLISECONDS)).isGreaterThan(300);
		assertThat(raisedException.isPresent()).isTrue();
		assertThat(raisedException.get()).isInstanceOf(RetryFailedException.class);
		assertThat(raisedException.get()).isInstanceOf(RetryFailedException.class);
		RetryFailedException retryFailedException = (RetryFailedException) raisedException.get();
		assert retryFailedException != null;
		assertThat(retryFailedException.getErrors()).containsExactly(exception, exception, exception);
		assertThat(retryFailedException.getRetryState().attempt()).isEqualTo(4);
		assertThat(retryFailedException.getRetryState().timeout()).isEqualTo(Duration.ofMillis(300));
	}
}
