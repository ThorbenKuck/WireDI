package com.github.thorbenkuck.di.annotations.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method should not be proxied
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Pure {
}
