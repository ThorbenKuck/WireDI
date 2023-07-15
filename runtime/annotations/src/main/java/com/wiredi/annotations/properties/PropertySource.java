package com.wiredi.annotations.properties;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface PropertySource {

    String[] value() default {};

    Entry[] entries() default {};
}
