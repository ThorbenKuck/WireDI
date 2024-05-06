package com.wiredi.runtime.async.state;

import org.jetbrains.annotations.Nullable;

/**
 * A read-only state implementation.
 *
 * @param <T>
 */
public class ReadOnlyState<T> extends AbstractState<T> {

    public ReadOnlyState(@Nullable T value) {
        super(value);
    }
}
