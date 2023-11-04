package com.wiredi.lang.time;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Timed {

    public static final Timed ZERO = new Timed(0);
    private final long nanos;

    Timed(long nanos) {
        this.nanos = nanos;
    }

    Timed(Duration duration) {
        this(duration.toNanos());
    }

    public static Timed of(Duration duration) {
        return new Timed(duration);
    }

    public static Timed of(Runnable runnable) {
        long nanos = System.nanoTime();
        runnable.run();
        return new Timed(System.nanoTime() - nanos);
    }

    public static <T> TimedValue<T> of(Supplier<T> supplier) {
        return TimedValue.get(supplier);
    }

    public Timed plus(Timed timed) {
        long newNanos = nanos + timed.nanos;
        return new Timed(newNanos);
    }

    public Timed plus(Duration duration) {
        long newNanos = nanos + duration.toNanos();
        return new Timed(newNanos);
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
