package com.wiredi.runtime.retry.backoff;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * No back off
 */
public class NoBackOffStrategy extends BackOffStrategy<NoBackOffStrategy> {

    @NotNull
    public static final NoBackOffStrategy INSTANCE = new NoBackOffStrategy();

    @Override
    @NotNull
    protected Duration calculateNext(@NotNull final Duration duration) {
        return Duration.ZERO;
    }
}
