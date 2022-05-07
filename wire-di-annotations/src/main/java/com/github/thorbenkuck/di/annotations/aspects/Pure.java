package com.github.thorbenkuck.di.annotations.aspects;

import java.lang.annotation.*;

/**
 * Indicates that the annotated method should not be proxied, or an annotated class is not eligible for proxy creation.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface Pure {
}
