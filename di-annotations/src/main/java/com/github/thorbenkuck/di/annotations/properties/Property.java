package com.github.thorbenkuck.di.annotations.properties;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
@Documented
public @interface Property {

    /**
     * The name of the property, as maintained in the WiredTypes.
     */
    String value();

    String defaultValue() default "";

}
