package com.wiredi.runtime;

import com.wiredi.runtime.domain.Eager;

import java.util.List;

/**
 * This interface abstracts strategies on how {@link Eager} instances are loaded.
 * <p>
 * Custom implementations can be provided to override the strategy on how eager classes are initialized.
 * The {@link WireRepository} tries to fetch a single instance from the BeanContainer.
 * If it finds a single instance, it's used.
 * Otherwise
 */
public interface EagerInitializer {

    /**
     * Initialize all eager instances.
     * <p>
     * It's expected that all {@link Eager#setup(WireRepository)} methods are called.
     *
     * @param eagerInstances the {@link Eager} instances to setup
     * @param wireRepository the repository which asks for initialization
     */
    void initialize(WireRepository wireRepository, List<Eager> eagerInstances);

    class ParallelStream implements EagerInitializer {

        @Override
        public void initialize(WireRepository wireRepository, List<Eager> eagerInstances) {
            eagerInstances.parallelStream().forEach(it -> it.setup(wireRepository));
        }
    }
}
