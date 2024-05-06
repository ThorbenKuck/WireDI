package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.Conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
@Conditional(ConditionalOnPropertyEvaluator.class)
public @interface ConditionalOnProperty {

    String key();

    String havingValue() default "";

    boolean matchIfMissing() default false;

}
