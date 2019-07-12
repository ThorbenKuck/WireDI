package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Wire {

	/**
	 * Defines which classes identify this wire type
	 *
	 * The annotated class has to be assignable to all classes provided here.
	 *
	 * @return all the types, the annotated type should be wired to
	 */
	Class<?>[] to() default {};

	boolean lazy() default true;

}
