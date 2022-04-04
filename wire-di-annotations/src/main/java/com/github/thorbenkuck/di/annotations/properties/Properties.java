package com.github.thorbenkuck.di.annotations.properties;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface Properties {

    String prefix() default "";

    String file() default "";

    Lifecycle lifecycle() default Lifecycle.RUNTIME;

    enum Lifecycle {
        COMPILE,
        RUNTIME
    }
}
