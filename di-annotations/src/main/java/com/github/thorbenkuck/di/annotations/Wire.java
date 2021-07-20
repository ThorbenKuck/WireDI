package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

/**
 * This annotations marks the annotated type to be picked up, analyzed and have an IdentifiableProvider instance constructed.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface Wire {

	/**
	 * Defines which classes identify this wire type, overriding the default behaviour.
	 *
 	 * If this field is not set, the wire will be assigned to the class annotated as well as any first level .
	 *
	 * <b>NOTE:</b> The annotated class has to be assignable to all classes provided in this fields here.
	 *
	 * @return all the types, the annotated type should be wired to
	 */
	Class<?>[] to() default {};

	boolean singleton() default true;

}

