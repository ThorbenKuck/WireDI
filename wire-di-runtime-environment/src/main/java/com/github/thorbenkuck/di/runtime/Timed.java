package com.github.thorbenkuck.di.runtime;

import com.google.common.base.Stopwatch;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Timed {

	private final long nanos;

	private Timed(long nanos) {
		this.nanos = nanos;
	}

	private Timed(Duration duration) {
		this(duration.toNanos());
	}

	public static final Map<TimeUnit, String> HUMAN_READABLE = new HashMap<>();

	static {
		HUMAN_READABLE.put(TimeUnit.DAYS, "d");
		HUMAN_READABLE.put(TimeUnit.HOURS, "h");
		HUMAN_READABLE.put(TimeUnit.MINUTES, "m");
		HUMAN_READABLE.put(TimeUnit.SECONDS, "s");
		HUMAN_READABLE.put(TimeUnit.MILLISECONDS, "ms");
		HUMAN_READABLE.put(TimeUnit.MICROSECONDS, "\u33B2");
		HUMAN_READABLE.put(TimeUnit.NANOSECONDS, "ns");
	}

	public Timed plus(Timed timed) {
		long newNanos = nanos + timed.nanos;
		return new Timed(newNanos);
	}

	public static Timed of(Stopwatch stopwatch) {
		return new Timed(stopwatch.elapsed());
	}

	public static Timed of(Duration duration) {
		return new Timed(duration);
	}

	public static Timed of(Runnable runnable) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		runnable.run();
		stopwatch.stop();
		return Timed.of(stopwatch);
	}

	public static Timed empty() {
		return of(Duration.ZERO);
	}

	public long get(TimeUnit timeUnit) {
		return timeUnit.convert(nanos, TimeUnit.NANOSECONDS);
	}

	public String toString(TimeUnit timeUnit) {
		return get(timeUnit) + HUMAN_READABLE.get(timeUnit);
	}

	@Override
	public String toString() {
		long milliseconds = get(TimeUnit.MILLISECONDS);
		if (milliseconds == 0){
			long micro = get(TimeUnit.MICROSECONDS);
			if(micro == 0) {
				return get(TimeUnit.NANOSECONDS) + HUMAN_READABLE.get(TimeUnit.NANOSECONDS);
			} else {
				return micro + HUMAN_READABLE.get(TimeUnit.MICROSECONDS);
			}
		}
		return new TimedRenderer()
				.append(TimeUnit.MINUTES)
				.append(TimeUnit.SECONDS)
				.append(TimeUnit.MILLISECONDS)
				.toString();
	}

	class TimedRenderer {
		private final StringBuilder result = new StringBuilder();

		public TimedRenderer append(TimeUnit timeUnit) {
			long done = get(timeUnit);
			if(done == 0) {
				return this;
			}

			if(!result.isEmpty()) {
				result.append(", ");
			}

			result.append(done).append(HUMAN_READABLE.get(timeUnit));
			return this;
		}

		@Override
		public String toString() {
			return result.toString();
		}
	}
}
