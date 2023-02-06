package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.runtime.WireRepository;

/**
 * A class, implementing this interface, will be called after all beans have been created.
 * <p>
 * It differs from @PostConstruct, in that the method {@link #setup(WireRepository)} will be called after all classes
 * where successfully constructed.
 * <p>
 * Further, you cannot rely on execution order of instances. All
 */
public interface Eager {

    void setup(WireRepository wireRepository);

}
