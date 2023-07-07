package com.wiredi.lang.time;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record TimedValue<T>(T value, Timed time) {

	public static <T> TimedValue<T> get(Supplier<T> supplier) {
		final Stopwatch stopwatch = Stopwatch.started();
		final T result = supplier.get();
		stopwatch.stop();
		return new TimedValue<>(result, Timed.of(stopwatch));
	}

	public static <T> TimedValue<T> just(T t) {
		return new TimedValue<>(t, Timed.empty());
	}

	public TimedValue<T> then(Consumer<TimedValue<T>> consumer) {
		consumer.accept(this);
		return this;
	}
}
