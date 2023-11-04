package com.wiredi.domain.conditional.builtin;

import com.wiredi.domain.conditional.Conditional;

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
