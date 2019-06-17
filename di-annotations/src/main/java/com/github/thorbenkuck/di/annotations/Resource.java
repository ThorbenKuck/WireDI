package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Resource {

	/**
	 * The origin, where the resource should be searched.
	 *
	 * @return the origin of the resource
	 */
	Origin origin() default Origin.SYSTEM_PROPERTIES;

	/**
	 * The key of the resource that should be injected.
	 * <p>
	 * If not set (i.e. empty), the name of the annotated
	 * field should be used.
	 * </p>
	 *
	 * @return the key of this resource
	 */
	String key() default "";

	/**
	 * The value, that should be used, if it cannot be found
	 * within the origin.
	 *
	 * @return the default value
	 */
	String defaultValue() default "";

	enum Origin {
		/**
		 * This Origin means, that the generated class should
		 * use.
		 * <code>
		 * String value = System.getProperty(name)
		 * </code>
		 */
		SYSTEM_PROPERTIES,
		/**
		 * This Origin means, that the generated class should
		 * not use any custom means to find the property.
		 *
		 * This is mostly useless now. I get it.
		 */
		NONE;
	}
}
