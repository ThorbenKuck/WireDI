package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.runtime.WireRepository;

/**
 * A ContextCallback is a callback that is integrated into the startup lifecycle of the {@link WireRepository}
 *
 *
 *
 * Must have a single public constructor with no dependencies.
 *
 * This context is independent of the actual wiring.
 */
public interface ContextCallback {

    default void preLoading(WireRepository wireRepository) {}

    default void postLoading(WireRepository wireRepository) {}

    default void postAspectLoading(WireRepository wireRepository) {}

}
