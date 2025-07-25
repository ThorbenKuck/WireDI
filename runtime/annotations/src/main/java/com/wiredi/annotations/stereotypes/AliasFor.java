package com.wiredi.annotations.stereotypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AliasFor {
    String value();

    Class<?> nullType() default Void.class;

}
