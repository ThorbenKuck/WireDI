package com.wiredi.annotations.properties;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Inherited
@Documented
public @interface Property {

	String name() default "";

	String defaultValue() default "";

}
