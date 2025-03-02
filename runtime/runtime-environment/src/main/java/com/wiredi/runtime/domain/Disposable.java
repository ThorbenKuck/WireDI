package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireRepository;

/**
 * Classes that implement this interface will be used while {@link WireRepository#clear()} is called.
 * <p>
 * This implies that your instance can shut down and cleanup resources.
 */
public interface Disposable {

    /**
     * TearDown this instance.
     *
     * @param origin The WireRepository that is destroyed.
     */
    void tearDown(WireRepository origin);

}
