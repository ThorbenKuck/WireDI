package com.wiredi.lang.time;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

class StopwatchTest {

	private static final Percentage STOP_WATCH_PRECISION = Percentage.withPercentage(99.999999999999999999);
	private static final long millis = 10;
	private static final long expectedNanos = TimeUnit.MILLISECONDS.toNanos(millis);

	@RepeatedTest(100)
	public void test() {
		// Arrange
		Stopwatch stopwatch = new Stopwatch();

		// Act
		stopwatch.run(this::sleep);

		// Assert
		assertThat(stopwatch.elapsed())
				.isGreaterThanOrEqualTo(expectedNanos)
				.isCloseTo(expectedNanos, STOP_WATCH_PRECISION);
	}

	@RepeatedTest(100)
	public void testWithContinuesStop() {
		// Arrange
		Stopwatch stopwatch = new Stopwatch();

		// Act
		stopwatch.run(this::sleep);
		stopwatch.run(this::sleep);

		// Assert
		assertThat(stopwatch.elapsed())
				.isGreaterThanOrEqualTo(expectedNanos * 2)
				.isCloseTo(expectedNanos * 2, STOP_WATCH_PRECISION);
	}

	private void sleep() {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}