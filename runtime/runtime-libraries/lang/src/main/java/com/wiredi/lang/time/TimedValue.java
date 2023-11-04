package com.wiredi.lang.time;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record TimedValue<T>(T value, Timed time) {

	public static <T> TimedValue<T> get(Supplier<T> supplier) {
		final long start = System.nanoTime();
		final T result = supplier.get();
		final long stop = System.nanoTime();
		return new TimedValue<>(result, new Timed(stop - start));
	}

	public static <T> TimedValue<T> just(T t) {
		return new TimedValue<>(t, Timed.ZERO);
	}

	public TimedValue<T> then(Consumer<TimedValue<T>> consumer) {
		consumer.accept(this);
		return this;
	}
}
