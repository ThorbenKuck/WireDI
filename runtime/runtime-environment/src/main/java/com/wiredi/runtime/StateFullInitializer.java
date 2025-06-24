package com.wiredi.runtime;

import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.domain.Eager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public interface StateFullInitializer {

    void initialize(
            @NotNull WireRepository wireRepository,
            @NotNull List<StateFull<?>> stateFulls,
            @Nullable Duration timeout
    );

    class ParallelStream implements StateFullInitializer {

        @Override
        public void initialize(
                @NotNull WireRepository wireRepository,
                @NotNull List<StateFull<?>> stateFulls,
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
