package com.wiredi.annotations.aspects;

import java.lang.annotation.*;

/**
 * Marks a method of any wired bean as a handler for an annotation.
 * <p>
 * TODO: Add support for properties and the other standard injection stuff
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Inherited
public @interface Aspect {

	/**
	 * The annotation, this aspect is meant to process
	 *
	 * @return the annotation that triggers this specific aspect
	 */
	Class<? extends Annotation> around();

}
