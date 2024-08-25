package com.wiredi.runtime.time;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public enum TimePrecision {

    NANOS {
        @Override
        public long now() {
            return System.nanoTime();
        }

        @Override
        public long calculateElapsedNanos(long startTime) {
            return System.nanoTime() - startTime;
        }

        @Override
        public Duration toDuration(long start, long stop) {
            return Duration.ofNanos(stop - start);
        }
    },
    MILLISECONDS {
        @Override
        public long now() {
            return System.currentTimeMillis();
        }

        @Override
        public long calculateElapsedNanos(long startTime) {
            return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() - startTime);
        }

        @Override
        public Duration toDuration(long start, long stop) {

            return Duration.ofMillis(stop - start);
        }
    };

    public abstract long now();

    public abstract long calculateElapsedNanos(long startTime);

    public abstract Duration toDuration(long start, long stop);

}
