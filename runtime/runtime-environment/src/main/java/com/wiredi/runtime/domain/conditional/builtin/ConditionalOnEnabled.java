package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.Conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
@Conditional(ConditionalOnEnabledEvaluator.class)
public @interface ConditionalOnEnabled {
    String value();

    boolean enabledByDefault() default true;
}
