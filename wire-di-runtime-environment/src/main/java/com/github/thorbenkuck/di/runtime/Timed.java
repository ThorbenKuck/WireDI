package com.github.thorbenkuck.di.runtime;

import com.google.common.base.Stopwatch;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
}
