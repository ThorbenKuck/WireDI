package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface Wire {

	/**
	 * Defines which classes identify this wire type
	 * <p>
	 * The annotated class has to be assignable to all classes provided here.
	 *
	 * @return all the types, the annotated type should be wired to
	 */
	Class<?>[] to() default {};

	boolean lazy() default true;

}

