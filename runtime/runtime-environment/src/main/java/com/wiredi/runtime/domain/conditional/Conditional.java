package com.wiredi.runtime.domain.conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
@Repeatable(Conditions.class)
public @interface Conditional {

    Class<? extends ConditionEvaluator> value();

}
