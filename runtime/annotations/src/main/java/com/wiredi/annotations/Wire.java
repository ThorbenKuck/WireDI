package com.wiredi.annotations;

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
     * Setting this field will control how the annotation processor behaves, based on the property 'wiredi.proxy-mode'.
     * This property can be set simply in your application properties file and expects an enum of the type {@link ProxyMode}.
     * <p>
     * Based on the proxy mode, either a proxy will be generated or not.
     * And in general, the annotation processor will respect if this field is set or not.
     * This way you can control the proxy generation for all classes, even without explicitly setting this field, while
     * still being able to override the proxy generation for specific classes.
     * <p>
     * Please note: This field can be ignored, even if it is explicitly set to true or false.
     * The behavior is controlled by the 'wiredi.proxy-mode' property.
     *
     * @see ProxyMode
     */
    boolean proxy() default false;

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

