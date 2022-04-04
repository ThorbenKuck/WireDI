package com.github.thorbenkuck.di.annotations.properties;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface PropertySource {

    String value();

    Source source() default Source.CLASSPATH;

    enum Source {
        CLASSPATH
    }
}
