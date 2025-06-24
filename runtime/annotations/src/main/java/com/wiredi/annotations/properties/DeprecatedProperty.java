package com.wiredi.annotations.properties;

import com.wiredi.annotations.DeprecationLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface DeprecatedProperty {

    String reason();

    DeprecationLevel level() default DeprecationLevel.WARNING;

    String replacement() default "";

    String since() default "";
}
