package com.wiredi.annotations;

import java.lang.annotation.*;

/**
 * This annotation marks a method that should be invoked after a bean is constructed.
 * <p>
 * I behave the same way as {@link jakarta.annotation.PostConstruct} does, though it gives more options.
 * For example, You can mark invocations of Initialize methods as asynchronous.
 * <p>
 * If a method has both this annotation and {@link jakarta.annotation.PostConstruct},
 * {@link jakarta.annotation.PostConstruct} has a higher precedence.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@Documented
public @interface Initialize {

    boolean async() default true;

}
