package com.wiredi.runtime.scheduler;

import com.wiredi.runtime.lang.ThrowingRunnable;

public interface Task<E extends Throwable> extends ThrowingRunnable<E> {

    default void completed() {
    }

    default void started() {
    }

    default void handleError(Throwable throwable) {
    }

}
