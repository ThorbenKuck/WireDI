package com.github.thorbenkuck.di.annotations.properties;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Inherited
public @interface PropertyName {

    String value();

    /**
     * If true, the provided value will be formatted in accordance to the standard.
     *
     * If false, the value will be treated "as is" and not be adjusted for the property access.
     *
     * @return Whether the value should be formatted or used "as is".
     */
    boolean format() default true;

}
