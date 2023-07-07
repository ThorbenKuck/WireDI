package com.wiredi.lang.time;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Timed {

	private final long nanos;

	private Timed(long nanos) {
		this.nanos = nanos;
	}

	private Timed(Duration duration) {
		this(duration.toNanos());
	}

	public Timed plus(Timed timed) {
		long newNanos = nanos + timed.nanos;
		return new Timed(newNanos);
	}

	public Timed plus(Duration duration) {
		long newNanos = nanos + duration.toNanos();
		return new Timed(newNanos);
	}

	public static Timed of(Stopwatch stopwatch) {
		return new Timed(stopwatch.elapsed());
	}

	public static Timed of(Duration duration) {
		return new Timed(duration);
	}

	public static Timed of(Runnable runnable) {
		Stopwatch stopwatch = Stopwatch.started();
		runnable.run();
		stopwatch.stop();
		return of(stopwatch);
	}

	public static <T> TimedValue<T> of(Supplier<T> supplier) {
		return TimedValue.get(supplier);
	}

	public static Timed empty() {
		return of(Duration.ZERO);
	}

	public long get(TimeUnit timeUnit) {
		return timeUnit.convert(nanos, TimeUnit.NANOSECONDS);
	}

	public String toString(TimeUnit timeUnit) {
		return new TimeRenderer(nanos).append(timeUnit).toString();
	}

	public Timed then(Consumer<Timed> consumer) {
		consumer.accept(this);
		return this;
	}

	@Override
	public String toString() {
		return new TimeRenderer(nanos)
				.append(TimeUnit.DAYS)
				.append(TimeUnit.HOURS)
				.append(TimeUnit.MINUTES)
				.append(TimeUnit.SECONDS)
				.append(TimeUnit.MILLISECONDS)
				.appendIf(TimeUnit.MICROSECONDS, timeRenderer -> timeRenderer.get(TimeUnit.MILLISECONDS) == 0)
				.appendIf(TimeUnit.NANOSECONDS, timeRenderer -> timeRenderer.get(TimeUnit.MILLISECONDS) == 0)
				.toString();
	}

}
