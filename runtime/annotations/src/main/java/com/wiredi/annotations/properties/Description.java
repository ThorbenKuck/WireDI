package com.wiredi.annotations.properties;

import java.lang.annotation.*;

/**
 * A general description for the annotated field
 */
@Documented
@Inherited
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface Description {
    String value();
}
