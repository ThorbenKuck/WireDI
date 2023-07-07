package com.wiredi.lang.time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class TimeRenderer {

	private final Long nanos;
	private Long runningNanos;
	private final StringBuilder result = new StringBuilder();

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

	public TimeRenderer(Long nanos) {
		this.nanos = nanos;
		this.runningNanos = nanos;
	}

	public long get(TimeUnit timeUnit) {
		return timeUnit.convert(nanos, TimeUnit.NANOSECONDS);
	}

	public long getRemaining(TimeUnit timeUnit) {
		return timeUnit.convert(runningNanos, TimeUnit.NANOSECONDS);
	}

	public TimeRenderer append(TimeUnit timeUnit) {
		long done = getRemaining(timeUnit);
		if (done == 0) {
			return this;
		}

		runningNanos -= timeUnit.toNanos(done);

		if (!result.isEmpty()) {
			result.append(", ");
		}

		result.append(done).append(HUMAN_READABLE.get(timeUnit));
		return this;
	}

	public TimeRenderer appendIf(TimeUnit timeUnit, Predicate<TimeRenderer> timeRendererPredicate) {
		if (timeRendererPredicate.test(this)) {
			append(timeUnit);
		}

		return this;
	}

	@Override
	public String toString() {
		return result.toString();
	}

}
