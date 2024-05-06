package com.wiredi.runtime.domain.conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
public @interface Conditions {

    Conditional[] value();

}
