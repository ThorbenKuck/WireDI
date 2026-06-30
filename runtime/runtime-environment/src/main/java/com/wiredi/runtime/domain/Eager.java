package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;

/**
 * A class, implementing this interface, will be requested form the WireContainer after all beans have been created.
 * <p>
 * It differs from @PostConstruct, in that the method {@link #initialize(WireContainer)} will be called after all classes
 * where successfully constructed, whilst @PostConstruct methods will be called during the construction of the class,
 * inside the {@link IdentifiableProvider}.
 * <p>
 * Further, you cannot rely on the execution order of instances implementing the {@link #initialize(WireContainer)} method.
 * The function {@link #initialize(WireContainer)} will be called in a parallel stream.
 * If you require a sequential execution, you can use the {@link #setup()} method as a replacement and work with the
 * {@link com.wiredi.annotations.Order} annotation.
 */
public interface Eager {

    /**
     * Initialize this bean, based on a WireRepository.
     * <p>
     * This method is called after all beans have been created.
     * <p>
     * Since this method is invoked after the WireRepository is completely set up and all dependent classes have been
     * constructed, it is safe to use the {@link WireContainer} in this context.
     * <p>
     * This method is invoked by the {@link com.wiredi.runtime.EagerInitializer} after all beans have been created.
     * Commonly, each invocation is done in parallel to speed up the initialization process.
     * If you require a sequential execution, you can use the {@link #setup()} method as a replacement, or alter
     * the {@link com.wiredi.runtime.EagerInitializer} implementation.
     *
     * @param wireContainer the WireRepository the current bean is instantiated at.
     */
    default void initialize(WireContainer wireContainer) {
        // This method can be overwritten by implementations.
    }

    /**
     * Set up this bean.
     * <p>
     * This method is called directly after the bean has been created.
     * In its behavior, it is similar to a method annotated with {@link jakarta.annotation.PostConstruct}.
     * The invocation happens in the same thread as the bean creation and sequentially.
     */
    default void setup() {
        // This method can be overwritten by implementations.
    }
}
