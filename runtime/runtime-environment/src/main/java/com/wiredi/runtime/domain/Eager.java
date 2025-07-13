package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;

/**
 * A class, implementing this interface, will be called after all beans have been created.
 * <p>
 * It differs from @PostConstruct, in that the method {@link #setup(WireContainer)} will be called after all classes
 * where successfully constructed, whilst @PostConstruct methods will be called during the construction of the class,
 * inside the {@link IdentifiableProvider}.
 * <p>
 * Further, you cannot rely on execution order of instances. The function {@link #setup(WireContainer)} will be
 * called in a parallel stream.
 */
public interface Eager {

	/**
	 * Setup this bean, based on a WireRepository.
	 * <p>
	 * Since this method is invoked after the WireRepository is completely set up and all dependent classes have been
     * constructed, it is safe to use the {@link WireContainer} in this context.
	 *
	 * @param wireRepository the WireRepository the current bean is instantiated at.
	 */
	default void setup(WireContainer wireRepository) {
		// This method can be overwritten by implementations.
	}

}
