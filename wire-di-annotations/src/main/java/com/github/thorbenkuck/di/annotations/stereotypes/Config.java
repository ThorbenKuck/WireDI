package com.github.thorbenkuck.di.annotations.stereotypes;

import com.github.thorbenkuck.di.annotations.Wire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A config annotation may be used as a substitute for Wire, when wanting to mark a class as "only used for
 * configuration purposes".
 *
 * It behaves exactly like the Wire annotation itself, just using different semantics. A reading developer may easier
 * understand that this class is not meant to be used in the production code.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Wire(
		singleton = true,
		proxy = false
)
public @interface Config {
}
