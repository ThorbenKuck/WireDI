package com.github.thorbenkuck.di.annotations.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface PropertySource {

    String prefix() default "";

    String file() default "";

    Lifecycle lifecycle() default Lifecycle.RUNTIME;

    enum Lifecycle {
        COMPILE,
        RUNTIME
    }
}
