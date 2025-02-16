package com.wiredi.annotations.stereotypes;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.aspects.Pure;

import java.lang.annotation.*;

/**
 * A config annotation may be used as a substitute for Wire, when wanting to mark a class as "only used for
 * configuration".
 * <p>
 * It behaves exactly like the Wire annotation itself, just using different semantics.
 * A reading developer may understand it more easily that this class isn't meant to be used in the production code.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Wire(proxy = false)
@Pure
public @interface Configuration {
}
