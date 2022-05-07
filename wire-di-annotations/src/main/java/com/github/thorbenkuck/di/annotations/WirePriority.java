package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

/**
 * The priority of a Wired component, which is used when ordering by priority.
 *
 * This annotation will translate into a field of the IdentifiableProvider that is generated.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface WirePriority {

	int HIGHEST = Integer.MAX_VALUE;

	int LOWEST = Integer.MIN_VALUE;

	int DEFAULT = 0;

	int value();
}

