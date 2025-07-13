package com.wiredi.annotations;

import com.wiredi.annotations.aspects.AspectTarget;

import java.lang.annotation.*;

/**
 * This annotation marks the annotated type to be picked up, analyzed and have an IdentifiableProvider instance
 * constructed.
 * <p>
 * By default, all classes annotated with {@code @Wire} are picked up by one annotation processor and one
 * IdentifiableProvider is generated. Additionally, annotated classes might result in generated proxy classes, which
 * are used in place of the annotated class
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Wire {

    /**
     * Defines which classes identify this wire type, overriding the default behavior.
     * <p>
     * If this field is not set, the wire will be assigned to all Interface and/or
     * parent classes the annotated class inherits from.
     * <p>
     * <b>NOTE:</b> The annotated class has to be assignable to all classes provided in this field here.
     *
     * @return all the types, the annotated type should be wired to
     */
    Class<?>[] to() default {};

    /**
     * This field indicates whether the annotated class is eligible to generate proxies or not.
     * <p>
     * If true, the annotation processor will try to generate an aspect-aware proxy. Proxy generation strategies are
     * dependent on the concrete annotation processor. By default, an aspect-aware proxy will be generated, if the
     * annotated class has one method, that is annotated or has an annotated parameter with an annotation, that
     * itself is annotated with the {@link AspectTarget} annotation.
     * <p>
     * By contract, proxies should have a name ending on "<i>$$AspectAwareProxy</i>" and inherit from the marker
     * interface "AspectAwareProxy".
     *
     * @return true if the creation of a proxy should be attempted, or false if no proxy should ever be generated.
     * @see com.wiredi.annotations.aspects.Pure
     */
    boolean proxy() default true;

    /**
     * Marks either a class or provider function, as a primary wire type.
     * <p>
     * Primary means that if the context contains multiple unqualified beans,
     * the primary instance will be injected if requested.
     * <p>
     * Setting this value to true is the same as if the Class or Provider is annotated with {@link Primary}.
     *
     * @return true if the bean should override other beans of the same type.
     * @see Primary
     */
    boolean primary() default false;
}

