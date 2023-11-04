package com.wiredi.processor.tck;

import com.wiredi.domain.conditional.Conditional;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
@Conditional(TrueConditionEvaluator.class)
public @interface ConditionalOnTrue {

    boolean condition() default true;

}
