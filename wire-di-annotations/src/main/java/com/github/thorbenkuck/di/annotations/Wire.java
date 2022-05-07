package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

/**
 * This annotation marks the annotated type to be picked up, analyzed and have an IdentifiableProvider instance
 * constructed.
 *
 * By default, all classes annotated with {@literal @Wire} are picked up by one annotation processor and one
 * IdentifiableProvider is generated. Additionally, annotated classes might result in generated proxy classes, which
 * are used in place of the annotated class
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Wire {

	/**
	 * Defines which classes identify this wire type, overriding the default behaviour.
	 *
 	 * If this field is not set, the wire will be assigned to the class annotated as well as any Interface and/or
	 * parent classes the annotated class inherits from.
	 *
	 * <b>NOTE:</b> The annotated class has to be assignable to all classes provided in this field here.
	 *
	 * @return all the types, the annotated type should be wired to
	 */
	Class<?>[] to() default {};

	/**
	 * Whether the annotated class should be instantiated onl once, or every time it is requested in the context
	 * of the dependency container.
	 *
	 * By default, all wire candidates are treated as singleton classes
	 *
	 * @return true, if only one instance of this class should be created, false if you want one for every request
	 */
	boolean singleton() default true;

	/**
	 * This field indicates, whether the annotated class is eligible to generate proxies or not.
	 *
	 * If true, the annotation processor will try to generate an aspect aware proxy. Proxy generation strategies are
	 * dependent on the concrete annotation processor. By default, an aspect aware proxy will be generated, if the
	 * annotated class has one method, that is annotated or has an annotated parameter with an annotation, that
	 * itself is annotated with the {@link com.github.thorbenkuck.di.annotations.aspects.AspectTarget} annotation.
	 *
	 * By contract, proxies should have a name ending on "<i>$$AspectAwareProxy</i>" and inherit from the marker
	 * interface "AspectAwareProxy".
	 *
	 * @return true if the creation of a proxy should be attempted, or false if no proxy should ever be generated.
	 */
	boolean proxy() default true;

}

