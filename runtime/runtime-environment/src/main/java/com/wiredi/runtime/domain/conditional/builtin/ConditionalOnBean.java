package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.Conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
@Conditional(ConditionalOnBeanEvaluator.class)
public @interface ConditionalOnBean {

    Class<?> type();

}
