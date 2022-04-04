package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.WireRepository;

/**
 * Must have a single public constructor with no dependencies.
 *
 * This context is independent of the actual wiring.
 */
public interface ContextCallback {

    default void preLoading(WireRepository wireRepository) {}

    default void postLoading(WireRepository wireRepository) {}

    default void postAspectLoading(WireRepository wireRepository) {}

}
