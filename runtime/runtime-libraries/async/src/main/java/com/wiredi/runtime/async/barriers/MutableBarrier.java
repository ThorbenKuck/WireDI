package com.wiredi.runtime.async.barriers;

import org.jetbrains.annotations.NotNull;

public interface MutableBarrier extends Barrier {

    void close();

    void open();

    @NotNull
    static MutableBarrier opened() {
        final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.open();
        return barrier;
    }

    @NotNull
    static MutableBarrier closed() {
        final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.close();
        return barrier;
    }
}
