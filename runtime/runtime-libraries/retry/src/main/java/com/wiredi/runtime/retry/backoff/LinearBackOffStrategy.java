package com.wiredi.runtime.retry.backoff;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A linear back off will always increment the previous duration with the fixed {@link #increment}.
 */
public class LinearBackOffStrategy extends BackOffStrategy<LinearBackOffStrategy> {

    @NotNull
    private final Duration increment;

    public LinearBackOffStrategy(@NotNull final Duration increment) {
        this.increment = increment;
    }

    @Override
    @NotNull
    protected Duration calculateNext(@NotNull final Duration duration) {
        return duration.plus(increment);
    }
}
