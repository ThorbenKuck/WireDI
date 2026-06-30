package com.wiredi.runtime.scheduler;

import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.util.concurrent.ScheduledFuture;

public interface Scheduler {

    @NotNull
    default Clock getClock() {
        return Clock.systemDefaultZone();
    }

    @NotNull
    <E extends Throwable> ScheduledFuture<?> schedule(@NotNull Task<E> task, @NotNull Trigger trigger);

}
