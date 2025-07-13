package com.wiredi.runtime;

import com.wiredi.runtime.async.StateFull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;

public interface StateFullInitializer {

    void initialize(
            @NotNull WireContainer wireRepository,
            @NotNull Collection<StateFull<?>> stateFulls,
            @Nullable Duration timeout
    );

    class ParallelStream implements StateFullInitializer {

        @Override
        public void initialize(
                @NotNull WireContainer wireRepository,
                @NotNull Collection<StateFull<?>> stateFulls,
                @Nullable Duration timeout
        ) {
            if (timeout != null) {
                stateFulls.parallelStream().forEach(it -> it.getState().awaitUntilSet(timeout));
            } else {
                stateFulls.parallelStream().forEach(StateFull::getState);
            }
        }
    }
}
