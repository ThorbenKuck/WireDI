package com.wiredi.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Order {

	/**
	 * The last entry in the order
	 */
	int LAST = Integer.MAX_VALUE;

	/**
	 * The first entry in the order
	 */
	int FIRST = Integer.MIN_VALUE;

	/**
	 * The default value
	 */
	int DEFAULT = 0;

	/**
	 * The order value for the annotated element (i.e method, or class).
	 *
	 * <p>Elements are ordered based on priority where a lower value has greater
	 * priority than a higher value. In an ordered Collection, the first element
	 * will be the one element with the lowest value, whilst the last element
	 * is the element with the greatest value.
	 *
	 * @see #DEFAULT
	 * @see #FIRST
	 * @see #LAST
	 */
	int value() default DEFAULT;

}