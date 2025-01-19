package com.wiredi.runtime.async.barriers;

import org.jetbrains.annotations.NotNull;

public interface MuitableBarrier extends Barrier {

    void close();

    void open();

    @NotNull
    static MuitableBarrier opened() {
        final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.open();
        return barrier;
    }

    @NotNull
    static MuitableBarrier closed() {
        final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.close();
        return barrier;
    }
}
